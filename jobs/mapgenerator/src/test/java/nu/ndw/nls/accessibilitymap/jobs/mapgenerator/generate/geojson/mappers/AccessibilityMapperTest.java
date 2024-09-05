package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessibilityMapperTest {

    private final AccessibilityMapper accessibilityMapper = new AccessibilityMapper();

    @Test
    void map_ok_nullNotAccessibleInBaseIsoChroneResult() {
        assertThat(accessibilityMapper.map(null)).isFalse();
    }

    @Test
    void map_ok_falseNotAccessibleDueToRestrictionsResultOfIsoChrone() {
        assertThat(accessibilityMapper.map(false)).isFalse();
    }

    @Test
    void map_ok_trueAccessible() {
        assertThat(accessibilityMapper.map(true)).isTrue();
    }
}