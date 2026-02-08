package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import ch.qos.logback.classic.Level;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlackCodeMapperTest {

    private BlackCodeMapper blackCodeMapper;

    private TrafficSignGeoJsonDto trafficSignGeoJsonDto;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        trafficSignGeoJsonDto = TrafficSignGeoJsonDto.builder()
                .id(UUID.randomUUID())
                .properties(TrafficSignPropertiesDto.builder()
                        .roadSectionId(123L)
                        .rvvCode(TrafficSignType.C1.getRvvCode())
                        .build())
                .build();

        blackCodeMapper = new BlackCodeMapper();
    }

    @Test
    void map() {

        trafficSignGeoJsonDto.getProperties().setBlackCode("4.1");

        assertThat(blackCodeMapper.map(trafficSignGeoJsonDto, TrafficSignType.C1)).isEqualTo(4.1d);
    }

    @Test
    void map_invalidValue() {

        trafficSignGeoJsonDto.getProperties().setBlackCode("4.1s");

        assertThat(blackCodeMapper.map(trafficSignGeoJsonDto, TrafficSignType.C1)).isNull();

        loggerExtension.containsLog(
                Level.WARN,
                "Unprocessable value %s for traffic sign with id %s and RVV code %s on road section %s".formatted(
                        trafficSignGeoJsonDto.getProperties().getBlackCode(),
                        trafficSignGeoJsonDto.getId(),
                        trafficSignGeoJsonDto.getProperties().getRvvCode(),
                        trafficSignGeoJsonDto.getProperties().getRoadSectionId()),
                "For input string: \"%s\"".formatted(trafficSignGeoJsonDto.getProperties().getBlackCode()));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void map_emptyValue(String blackCode) {

        trafficSignGeoJsonDto.getProperties().setBlackCode(blackCode);

        assertThat(blackCodeMapper.map(trafficSignGeoJsonDto, TrafficSignType.C1)).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class, mode = Mode.INCLUDE, names = {"C17", "C18", "C19", "C20", "C21"})
    void map_blackCodeRequiredForTrafficSign(TrafficSignType trafficSignType) {

        trafficSignGeoJsonDto.getProperties().setRvvCode(trafficSignType.getRvvCode());
        trafficSignGeoJsonDto.getProperties().setBlackCode(null);

        assertThat(catchThrowable(() -> blackCodeMapper.map(trafficSignGeoJsonDto, trafficSignType)))
                .hasMessage("Traffic sign with id '%s' is not containing a black code but that is required for type '%s'".formatted(
                        trafficSignGeoJsonDto.getId(), trafficSignType))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class, mode = Mode.EXCLUDE, names = {"C17", "C18", "C19", "C20", "C21"})
    void map_blackCodeNotRequiredForTrafficSign(TrafficSignType trafficSignType) {

        trafficSignGeoJsonDto.getProperties().setRvvCode(trafficSignType.getRvvCode());
        trafficSignGeoJsonDto.getProperties().setBlackCode("4.1s");

        assertThat(blackCodeMapper.map(trafficSignGeoJsonDto, trafficSignType)).isNull();
    }
}
