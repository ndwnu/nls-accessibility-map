package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.LOCAL_TRAFFIC;
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
            CHARGING,           null
            LOADING,            null
            PERMIT,             null
            LOCAL_TRAFFIC,      LOCAL_TRAFFIC,
            DISABLED_TRANSPORT, null,
            DANGEROUS_SUPPLIES, null
            """, nullValues = "null")
    void map(CategoryEnum sourceCategory, Category targetCategory) {
        assertThat(categoryMapper.map(sourceCategory)).isEqualTo(targetCategory);
    }

    @Test
    void getValue() {
        assertThat(LOCAL_TRAFFIC.getValue()).isEqualTo("Local traffic");

    }

}