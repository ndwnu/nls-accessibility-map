package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
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

}