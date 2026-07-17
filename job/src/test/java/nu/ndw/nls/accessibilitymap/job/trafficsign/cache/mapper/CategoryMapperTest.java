package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.CHARGING;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.DANGEROUS_SUPPLIES;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.DISABLED_TRANSPORT;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.LOADING;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.LOCAL_TRAFFIC;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.PERMIT;
import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CategoryMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();

    @ParameterizedTest
    @CsvSource(textBlock = """
            null,               null
            CHARGING,           CHARGING
            LOADING,            LOADING
            PERMIT,             PERMIT
            LOCAL_TRAFFIC,      LOCAL_TRAFFIC,
            DISABLED_TRANSPORT, DISABLED_TRANSPORT,
            DANGEROUS_SUPPLIES, DANGEROUS_SUPPLIES
            """, nullValues = "null")
    void map(CategoryEnum sourceCategory, Category targetCategory) {
        assertThat(categoryMapper.map(sourceCategory)).isEqualTo(targetCategory);
    }

    @Test
    void getValue() {
        assertThat(CHARGING.getValue()).isEqualTo("Charging");
        assertThat(LOADING.getValue()).isEqualTo("Loading");
        assertThat(PERMIT.getValue()).isEqualTo("Permit");
        assertThat(LOCAL_TRAFFIC.getValue()).isEqualTo("Local traffic");
        assertThat(DISABLED_TRANSPORT.getValue()).isEqualTo("Disabled transport");
        assertThat(DANGEROUS_SUPPLIES.getValue()).isEqualTo("Dangerous supplies");
    }
}
