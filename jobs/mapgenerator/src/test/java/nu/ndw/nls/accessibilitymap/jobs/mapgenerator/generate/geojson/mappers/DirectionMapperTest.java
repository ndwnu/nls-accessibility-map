package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import org.junit.jupiter.api.Test;

class DirectionMapperTest {

    private final DirectionMapper directionMapper = new DirectionMapper();

    @Test
    void map_ok() {
        assertThat(directionMapper.map(true)).isEqualTo(Direction.FORWARD);
        assertThat(directionMapper.map(false)).isEqualTo(Direction.BACKWARD);
        assertThat(Direction.values().length).isEqualTo(2);
    }
}
