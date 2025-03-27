package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers;

import static org.assertj.core.api.Assertions.assertThat;
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
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.geojson.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

@SuppressWarnings("ALL")
@ExtendWith(MockitoExtension.class)
class TrafficSignMapperTest {

    private TrafficSignMapper trafficSignMapper;

    private TrafficSignGeoJsonDto trafficSignGeoJsonDto;

    private IntegerSequenceSupplier integerSequenceSupplier;

    @Mock
    private List<TextSign> textSigns;

    @Mock
    private TrafficSignRestrictionsBuilder trafficSignRestrictionsBuilder;

    @Mock
    private Restrictions restrictions;

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
                        .textSigns(textSigns)
                        .build())
                .geometry(new Point(3d, 4d))
                .build();
        trafficSignMapper = new TrafficSignMapper(trafficSignRestrictionsBuilder);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void mapFromTrafficSignGeoJsonDto(TrafficSignType trafficSignType) {

        when(trafficSignRestrictionsBuilder.buildFor(argThat(trafficSign -> trafficSign.trafficSignType() == trafficSignType)))
                .thenReturn(restrictions);

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

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        validateTrafficSign(trafficSign.get());
        loggerExtension.containsLog(Level.WARN,
                "Unprocessable value invalid for traffic sign with id %s and RVV code C6 on road section 1".formatted(
                        trafficSignGeoJsonDto.getId()));
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void mapFromTrafficSignGeoJsonDto_allDirections(DirectionType directionType) {

        if (directionType != DirectionType.BOTH) {
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

        if (trafficSignGeoJsonDto.getProperties().getBlackCode().equals("invalid")) {
            assertThat(trafficSign.blackCode()).isNull();
        } else {
            assertThat(trafficSign.blackCode()).isEqualTo(Double.parseDouble(trafficSignGeoJsonDto.getProperties().getBlackCode()));
        }
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
