package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass.EURO_1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass.EURO_2;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass.EURO_3;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass.EURO_4;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass.EURO_5;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass.EURO_6;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EuroClassificationMapperTest {

    private final EuroClassificationMapper euroClassificationMapper = new EuroClassificationMapper();

    @Test
    void map_validValues() {
        assertThat(euroClassificationMapper.map(null)).isNull();
        assertThat(euroClassificationMapper.map(1)).isEqualTo(EURO_1);
        assertThat(euroClassificationMapper.map(2)).isEqualTo(EURO_2);
        assertThat(euroClassificationMapper.map(3)).isEqualTo(EURO_3);
        assertThat(euroClassificationMapper.map(4)).isEqualTo(EURO_4);
        assertThat(euroClassificationMapper.map(5)).isEqualTo(EURO_5);
        assertThat(euroClassificationMapper.map(6)).isEqualTo(EURO_6);
    }

    @Test
    void map_invalidValue() {
        assertThat(euroClassificationMapper.map(0)).isEqualTo(UNKNOWN);
        assertThat(euroClassificationMapper.map(7)).isEqualTo(UNKNOWN);

    }
}