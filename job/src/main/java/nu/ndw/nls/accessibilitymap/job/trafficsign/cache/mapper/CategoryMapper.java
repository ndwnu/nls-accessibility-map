package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.LOCAL_TRAFFIC;


import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category map(CategoryEnum categoryEnum) {

        if (categoryEnum == null) {
            return null;
        }

        return switch(categoryEnum) {
            case CHARGING -> null;
            case LOADING -> null;
            case PERMIT -> null;
            case LOCAL_TRAFFIC -> LOCAL_TRAFFIC;
            case DISABLED_TRANSPORT -> null;
            case DANGEROUS_SUPPLIES -> null;
        };
    }

}
