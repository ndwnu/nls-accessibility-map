package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.DrivingDirectionEnum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DirectionMapperTest {

    private final DirectionMapper directionMapper = new DirectionMapper();

    @ParameterizedTest
    @CsvSource(textBlock = """
            null,   null
            FORTH,  FORWARD
            BACK,   BACKWARD
            """, nullValues = "null")
    void map(DrivingDirectionEnum drivingDirection, Direction direction) {
        assertThat(directionMapper.map(drivingDirection)).isEqualTo(direction);
    }
}

