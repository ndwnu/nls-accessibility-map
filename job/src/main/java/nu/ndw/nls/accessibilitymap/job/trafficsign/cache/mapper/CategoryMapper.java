package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.CHARGING;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.DANGEROUS_SUPPLIES;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.DISABLED_TRANSPORT;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.LOADING;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.LOCAL_TRAFFIC;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.PERMIT;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category map(CategoryEnum categoryEnum) {

        if (categoryEnum == null) {
            return null;
        }

        return switch (categoryEnum) {
            case CHARGING -> CHARGING;
            case LOADING -> LOADING;
            case PERMIT -> PERMIT;
            case LOCAL_TRAFFIC -> LOCAL_TRAFFIC;
            case DISABLED_TRANSPORT -> DISABLED_TRANSPORT;
            case DANGEROUS_SUPPLIES -> DANGEROUS_SUPPLIES;
        };
    }
}
