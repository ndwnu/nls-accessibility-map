package nu.ndw.nls.accessibilitymap.job.speedlimits.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.DirectionEnumJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionDirectionalSpeedLimitJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionSpeedLimitJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpeedLimitMapperTest {

    private SpeedLimitMapper speedLimitMapper;

    @BeforeEach
    void setUp() {

        speedLimitMapper = new SpeedLimitMapper();
    }

    @ParameterizedTest
    @EnumSource(DirectionEnumJson.class)
    void map(DirectionEnumJson direction) {
        RoadSectionSpeedLimitJson roadSectionSpeedLimitJson = new RoadSectionSpeedLimitJson()
                .nwbRoadSectionId(1234567890L)
                .directionalSpeedLimit(List.of(
                        new RoadSectionDirectionalSpeedLimitJson()
                                .averageSpeedLimit(50)
                                .direction(direction)
                ));

        SpeedLimit speedLimit = speedLimitMapper.map(
                roadSectionSpeedLimitJson,
                roadSectionSpeedLimitJson.getDirectionalSpeedLimit().getFirst());

        assertThat(speedLimit.roadSectionId()).isEqualTo(1234567890L);
        assertThat(speedLimit.direction()).isEqualTo(Direction.valueOf(direction.name()));
        assertThat(speedLimit.speedInKmPerHour()).isEqualTo(50);
    }
}
