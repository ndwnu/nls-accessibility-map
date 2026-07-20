package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FuelTypeMapperTest {

    private final FuelTypeMapper fuelTypeMapper = new FuelTypeMapper();

    @Test
    void map_null() {
        assertThat(fuelTypeMapper.map(null))
                .isNull();
    }

    @Test
    void map_nonNull_returnNull() {
        assertThat(fuelTypeMapper.map("other"))
                .isNull();
    }
}