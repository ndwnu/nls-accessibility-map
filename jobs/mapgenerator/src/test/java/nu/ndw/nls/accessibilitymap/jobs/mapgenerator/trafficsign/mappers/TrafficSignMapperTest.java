package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.IntegerSequenceSupplier;
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
        trafficSignMapper = new TrafficSignMapper();
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void mapFromTrafficSignGeoJsonDto_ok(TrafficSignType trafficSignType) {

        trafficSignGeoJsonDto.getProperties().setRvvCode(trafficSignType.getRvvCode());

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        validateTrafficSign(trafficSign.get());
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void mapFromTrafficSignGeoJsonDto_allDirections(DirectionType directionType) {

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

        trafficSignGeoJsonDto.getProperties().setImageUrl(imageUrl);

        Optional<TrafficSign> trafficSign = trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                trafficSignGeoJsonDto,
                integerSequenceSupplier);

        assertThat(trafficSign.get().iconUri()).isNull();
    }

    @ParameterizedTest
    @NullSource
    void mapFromTrafficSignGeoJsonDto_blackCode_null(String blackCode) {

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
        assertThat(trafficSign.roadSectionId()).isEqualTo(
                trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue());
        assertThat(trafficSign.trafficSignType()).
                isEqualTo(TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode()));
        assertThat(trafficSign.direction())
                .isEqualTo(createDirection(trafficSignGeoJsonDto.getProperties().getDrivingDirection()));
        assertThat(trafficSign.fraction()).isEqualTo(trafficSignGeoJsonDto.getProperties().getFraction());
        assertThat(trafficSign.latitude())
                .isEqualTo(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLatitude());
        assertThat(trafficSign.longitude())
                .isEqualTo(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLongitude());
        assertThat(trafficSign.textSigns()).isEqualTo(trafficSignGeoJsonDto.getProperties().getTextSigns());
        assertThat(trafficSign.blackCode()).isEqualTo(
                Double.parseDouble(trafficSignGeoJsonDto.getProperties().getBlackCode()));
        assertThat(trafficSign.iconUri()).isEqualTo(URI.create(trafficSignGeoJsonDto.getProperties().getImageUrl()));
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
