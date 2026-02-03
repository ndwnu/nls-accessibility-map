package nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.NwbRoadSectionSnapService;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache.mapper.BlackCodeMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

@SuppressWarnings("ALL")
@ExtendWith(MockitoExtension.class)
class TrafficSignBuilderTest {

    private static final double DEFAULT_X_COORDINATE = 1d;

    private static final double DEFAULT_Y_COORDINATE = 2d;

    private TrafficSignBuilder trafficSignBuilder;

    private TrafficSignGeoJsonDto trafficSignGeoJsonDto;

    private AtomicInteger idSequenceSupplier;

    @Mock
    private NwbRoadSectionSnapService nwbRoadSectionSnapService;

    @Mock
    private BlackCodeMapper blackCodeMapper;

    @Mock
    private List<TextSign> textSigns;

    @Mock
    private TrafficSignRestrictionsBuilder trafficSignRestrictionsBuilder;

    @Mock
    private TransportRestrictions transportRestrictions;

    @Mock
    private CoordinateAndBearing coordinateAndBearing;

    @Mock
    private Coordinate coordinate;

    @Mock
    private LineString nwbRoadSectionGeometry;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        idSequenceSupplier = new AtomicInteger(1);
        trafficSignGeoJsonDto = TrafficSignGeoJsonDto.builder()
                .id(UUID.randomUUID())
                .properties(TrafficSignPropertiesDto.builder()
                        .roadSectionId(1L)
                        .rvvCode("C6")
                        .drivingDirection(DirectionType.BACK)
                        .fraction(2d)
                        .blackCode("4.1")
                        .imageUrl("https://example.com/image")
                        .zoneCode("ZE")
                        .textSigns(textSigns)
                        .build())
                .geometry(new PointJson().type(TypeEnum.POINT).coordinates(List.of(DEFAULT_X_COORDINATE, DEFAULT_Y_COORDINATE)))
                .build();
        trafficSignBuilder = new TrafficSignBuilder(trafficSignRestrictionsBuilder, nwbRoadSectionSnapService, blackCodeMapper);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void mapFromTrafficSignGeoJsonDto(TrafficSignType trafficSignType) {

        when(blackCodeMapper.map(trafficSignGeoJsonDto, trafficSignType)).thenReturn(4.1d);
        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign -> trafficSign.trafficSignType() == trafficSignType)))
                .thenReturn(transportRestrictions);

        setupFixtureForNwbSnap();

        trafficSignGeoJsonDto.getProperties().setRvvCode(trafficSignType.getRvvCode());

        Optional<TrafficSign> trafficSign = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDto,
                idSequenceSupplier);

        validateTrafficSign(trafficSign.get());
    }

    @ParameterizedTest
    @NullSource
    void mapFromTrafficSignGeoJsonDto_noNwbRoadSectionGeometrySupplied(LineString nwbRoadSectionGeometry) {

        Optional<TrafficSign> trafficSign = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDto,
                idSequenceSupplier);

        assertThat(trafficSign).isEmpty();

        loggerExtension.containsLog(
                Level.INFO,
                "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: %s"
                        .formatted(trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto),
                "Traffic sign with id '%s' is missing a road section.".formatted(trafficSignGeoJsonDto.getId()));
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_missingFraction() {

        trafficSignGeoJsonDto.getProperties().setFraction(null);

        Optional<TrafficSign> trafficSign = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDto,
                idSequenceSupplier);

        assertThat(trafficSign).isEmpty();

        loggerExtension.containsLog(
                Level.INFO,
                "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: %s"
                        .formatted(trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto),
                "Traffic sign with id '%s' is missing a fraction.".formatted(trafficSignGeoJsonDto.getId()));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            ZE, END
            ZB, START
            ZH, REPEAT
            ZO, UNKNOWN
            """)
    void mapFromTrafficSignGeoJsonDto_allZoneCodeTypes(String zoneCodeString, String expectedZoneCodeType) {

        when(blackCodeMapper.map(trafficSignGeoJsonDto, TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
                .thenReturn(4.1d);
        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
        ).thenReturn(transportRestrictions);

        trafficSignGeoJsonDto.getProperties().setZoneCode(zoneCodeString);
        setupFixtureForNwbSnap();
        Optional<TrafficSign> trafficSign = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDto,
                idSequenceSupplier);

        validateTrafficSign(trafficSign.get());
        assertThat(trafficSign.get().zoneCodeType()).isEqualTo(ZoneCodeType.valueOf(expectedZoneCodeType));
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_invalidZoneCodeType() {

        trafficSignGeoJsonDto.getProperties().setZoneCode("invalid");

        Optional<TrafficSign> trafficSign = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDto,
                idSequenceSupplier);

        assertThat(trafficSign).isEmpty();
        loggerExtension.containsLog(
                Level.INFO,
                "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: %s"
                        .formatted(trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto),
                "Unknown zone code 'invalid'");
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_invalidZoneCodeType_null() {

        when(blackCodeMapper.map(trafficSignGeoJsonDto, TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
                .thenReturn(4.1d);
        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
        ).thenReturn(transportRestrictions);

        trafficSignGeoJsonDto.getProperties().setZoneCode(null);
        setupFixtureForNwbSnap();

        Optional<TrafficSign> trafficSign = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDto,
                idSequenceSupplier);

        validateTrafficSign(trafficSign.get());
        assertThat(trafficSign.get().zoneCodeType()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void mapFromTrafficSignGeoJsonDto_allDirections(DirectionType directionType) {

        if (directionType != DirectionType.BOTH) {
            when(blackCodeMapper.map(trafficSignGeoJsonDto,
                    TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
                    .thenReturn(4.1d);
            setupFixtureForNwbSnap();
            when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                    trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
            ).thenReturn(transportRestrictions);
        }

        trafficSignGeoJsonDto.getProperties().setDrivingDirection(directionType);

        Optional<TrafficSign> trafficSign = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDto,
                idSequenceSupplier);

        if (directionType == DirectionType.BOTH) {
            assertThat(trafficSign).isEmpty();
            loggerExtension.containsLog(
                    Level.INFO,
                    "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: %s"
                            .formatted(trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto),
                    "Driving direction '%s' could not be mapped.".formatted(directionType));
        } else {
            validateTrafficSign(trafficSign.get());
        }
    }

    @ParameterizedTest
    @NullSource
    void mapFromTrafficSignGeoJsonDto_imageUrl_null(String imageUrl) {

        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
        ).thenReturn(transportRestrictions);

        trafficSignGeoJsonDto.getProperties().setImageUrl(imageUrl);
        setupFixtureForNwbSnap();
        Optional<TrafficSign> trafficSign = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDto,
                idSequenceSupplier);

        assertThat(trafficSign.get().iconUri()).isNull();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                trafficSignBuilder.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    private void validateTrafficSign(TrafficSign trafficSign) {

        assertThat(trafficSign.id()).isEqualTo(1);
        assertThat(trafficSign.roadSectionId()).isEqualTo(trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue());
        assertThat(trafficSign.trafficSignType())
                .isEqualTo(TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode()));
        assertThat(trafficSign.direction()).isEqualTo(createDirection(trafficSignGeoJsonDto.getProperties().getDrivingDirection()));
        assertThat(trafficSign.fraction()).isEqualTo(trafficSignGeoJsonDto.getProperties().getFraction());
        assertThat(trafficSign.latitude()).isEqualTo(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLast());
        assertThat(trafficSign.longitude()).isEqualTo(trafficSignGeoJsonDto.getGeometry().getCoordinates().getFirst());
        assertThat(trafficSign.textSigns()).isEqualTo(trafficSignGeoJsonDto.getProperties().getTextSigns());
        assertThat(trafficSign.iconUri()).isEqualTo(URI.create(trafficSignGeoJsonDto.getProperties().getImageUrl()));
        assertThat(trafficSign.transportRestrictions()).isEqualTo(transportRestrictions);
        assertThat(trafficSign.networkSnappedLatitude()).isEqualTo(DEFAULT_Y_COORDINATE);
        assertThat(trafficSign.networkSnappedLongitude()).isEqualTo(DEFAULT_X_COORDINATE);

        if (trafficSignGeoJsonDto.getProperties().getBlackCode().equals("invalid")) {
            assertThat(trafficSign.blackCode()).isNull();
        } else {
            assertThat(trafficSign.blackCode()).isEqualTo(Double.parseDouble(trafficSignGeoJsonDto.getProperties().getBlackCode()));
        }
    }

    private void setupFixtureForNwbSnap() {

        when(nwbRoadSectionSnapService.snapToLineForRdGeometry(nwbRoadSectionGeometry, trafficSignGeoJsonDto.getProperties().getFraction())).thenReturn(
                coordinateAndBearing);
        when(coordinateAndBearing.coordinate()).thenReturn(coordinate);
        when(coordinate.getX()).thenReturn(DEFAULT_X_COORDINATE);
        when(coordinate.getY()).thenReturn(DEFAULT_Y_COORDINATE);
    }

    private Direction createDirection(DirectionType drivingDirection) {

        switch (drivingDirection) {
            case FORTH -> {
                return Direction.FORWARD;
            }
            case BACK -> {
                return Direction.BACKWARD;
            }
            default -> {
                return null;
            }
        }
    }
}
