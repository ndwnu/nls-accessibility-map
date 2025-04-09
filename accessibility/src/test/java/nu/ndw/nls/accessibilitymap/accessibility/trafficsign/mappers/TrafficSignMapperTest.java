package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.accessibility.services.NwbRoadSectionSnapService;
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.geojson.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

@SuppressWarnings("ALL")
@ExtendWith(MockitoExtension.class)
class TrafficSignMapperTest {

    private static final double DEFAULT_X_COORDINATE = 1d;
    private static final double DEFAULT_Y_COORDINATE = 2d;
    private TrafficSignMapper trafficSignMapper;


    private TrafficSignGeoJsonDto trafficSignGeoJsonDto;

    private IntegerSequenceSupplier integerSequenceSupplier;
    @Mock
    private NwbRoadSectionSnapService nwbRoadSectionSnapService;

    @Mock
    private List<TextSign> textSigns;

    @Mock
    private TrafficSignRestrictionsBuilder trafficSignRestrictionsBuilder;

    @Mock
    private Restrictions restrictions;

    @Mock
    private CoordinateAndBearing coordinateAndBearing;
    @Mock
    private Coordinate coordinate;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {
        integerSequenceSupplier = new IntegerSequenceSupplier();
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
                .geometry(new Point(3d, 4d))
                .build();
        trafficSignMapper = new TrafficSignMapper(trafficSignRestrictionsBuilder,nwbRoadSectionSnapService);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void mapFromTrafficSignGeoJsonDto(TrafficSignType trafficSignType) {

        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign -> trafficSign.trafficSignType() == trafficSignType)))
                .thenReturn(restrictions);

        setupFixtureForNwbSnap();

        trafficSignGeoJsonDto.getProperties().setRvvCode(trafficSignType.getRvvCode());

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        validateTrafficSign(trafficSign.get());
    }


    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class, mode = Mode.INCLUDE, names = {"C17", "C18", "C19", "C20", "C21"})
    void mapFromTrafficSignGeoJsonDto_blackCode_required_null(TrafficSignType trafficSignType) {

        trafficSignGeoJsonDto.getProperties().setRvvCode(trafficSignType.getRvvCode());
        trafficSignGeoJsonDto.getProperties().setBlackCode(null);

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        assertThat(trafficSign).isEmpty();

        loggerExtension.containsLog(
                Level.WARN,
                "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: %s"
                        .formatted(trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto),
                "Traffic sign with id '%s' is not containing a black code but that is required for type '%s'".formatted(
                        trafficSignGeoJsonDto.getId(), trafficSignType));

    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class, mode = Mode.INCLUDE, names = {"C17", "C18", "C19", "C20", "C21"})
    void mapFromTrafficSignGeoJsonDto_blackCode_required_invalid(TrafficSignType trafficSignType) {

        trafficSignGeoJsonDto.getProperties().setRvvCode(trafficSignType.getRvvCode());
        trafficSignGeoJsonDto.getProperties().setBlackCode("invalid");

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        assertThat(trafficSign).isEmpty();

        loggerExtension.containsLog(
                Level.WARN,
                "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: %s"
                        .formatted(trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto),
                "Traffic sign with id '%s' is not containing a black code but that is required for type '%s'".formatted(
                        trafficSignGeoJsonDto.getId(), trafficSignType));
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_invalidBlackCode() {

        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
        ).thenReturn(restrictions);

        trafficSignGeoJsonDto.getProperties().setBlackCode("invalid");
        setupFixtureForNwbSnap();
        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        validateTrafficSign(trafficSign.get());
        loggerExtension.containsLog(Level.WARN,
                "Unprocessable value invalid for traffic sign with id %s and RVV code C6 on road section 1".formatted(
                        trafficSignGeoJsonDto.getId()));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            ZE, END
            ZB, START
            ZH, REPEAT
            ZO, UNKNOWN
            """)
    void mapFromTrafficSignGeoJsonDto_allZoneCodeTypes(String zoneCodeString, String expectedZoneCodeType) {

        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
        ).thenReturn(restrictions);

        trafficSignGeoJsonDto.getProperties().setZoneCode(zoneCodeString);
        setupFixtureForNwbSnap();
        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        validateTrafficSign(trafficSign.get());
        assertThat(trafficSign.get().zoneCodeType()).isEqualTo(ZoneCodeType.valueOf(expectedZoneCodeType));
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_invalidZoneCodeType() {

        trafficSignGeoJsonDto.getProperties().setZoneCode("invalid");

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);


            assertThat(trafficSign).isEmpty();
            loggerExtension.containsLog(
                    Level.WARN,
                    "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: %s"
                            .formatted(trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto),
                    "Unknown zone code 'invalid'");
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_invalidZoneCodeType_null() {

        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
        ).thenReturn(restrictions);

        trafficSignGeoJsonDto.getProperties().setZoneCode(null);
        setupFixtureForNwbSnap();

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        validateTrafficSign(trafficSign.get());
        assertThat(trafficSign.get().zoneCodeType()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void mapFromTrafficSignGeoJsonDto_allDirections(DirectionType directionType) {

        if (directionType != DirectionType.BOTH) {
            setupFixtureForNwbSnap();
            when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                    trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
            ).thenReturn(restrictions);
        }

        trafficSignGeoJsonDto.getProperties().setDrivingDirection(directionType);

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        if (directionType == DirectionType.BOTH) {
            assertThat(trafficSign).isEmpty();
            loggerExtension.containsLog(
                    Level.WARN,
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
        ).thenReturn(restrictions);

        trafficSignGeoJsonDto.getProperties().setImageUrl(imageUrl);
        setupFixtureForNwbSnap();
        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        assertThat(trafficSign.get().iconUri()).isNull();
    }

    @ParameterizedTest
    @NullSource
    void mapFromTrafficSignGeoJsonDto_blackCode_null(String blackCode) {

        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign ->
                trafficSign.trafficSignType() == TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode())))
        ).thenReturn(restrictions);
        setupFixtureForNwbSnap();
        trafficSignGeoJsonDto.getProperties().setBlackCode(blackCode);

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        assertThat(trafficSign.get().blackCode()).isNull();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                trafficSignMapper.getClass(),
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
        assertThat(trafficSign.latitude()).isEqualTo(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLatitude());
        assertThat(trafficSign.longitude()).isEqualTo(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLongitude());
        assertThat(trafficSign.textSigns()).isEqualTo(trafficSignGeoJsonDto.getProperties().getTextSigns());
        assertThat(trafficSign.iconUri()).isEqualTo(URI.create(trafficSignGeoJsonDto.getProperties().getImageUrl()));
        assertThat(trafficSign.restrictions()).isEqualTo(restrictions);
        assertThat(trafficSign.networkSnappedLatitude()).isEqualTo(DEFAULT_Y_COORDINATE);
        assertThat(trafficSign.networkSnappedLongitude()).isEqualTo(DEFAULT_X_COORDINATE);
        if (trafficSignGeoJsonDto.getProperties().getBlackCode().equals("invalid")) {
            assertThat(trafficSign.blackCode()).isNull();
        } else {
            assertThat(trafficSign.blackCode()).isEqualTo(Double.parseDouble(trafficSignGeoJsonDto.getProperties().getBlackCode()));
        }
    }

    private void setupFixtureForNwbSnap() {
        when(nwbRoadSectionSnapService.snapTrafficSign(any()))
                .thenReturn(Optional.of(coordinateAndBearing));
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
        }

        return null;
    }

}
