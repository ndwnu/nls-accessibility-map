package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection.RoadSectionRestriction;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadSectionRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class RoadSectionRestrictionToRestrictionJsonMapperV2 implements AccessibilityRestrictionJsonMapperV2<RoadSectionRestriction> {

    @Override
    public RestrictionJson map(RoadSectionRestriction roadSectionRestriction) {
        return RoadSectionRestrictionJson.builder()
                .type(TypeEnum.ROAD_SECTION)
                .roadSectionId(roadSectionRestriction.roadSectionId().longValue())
                .build();
    }

    @Override
    public Class<? extends Restriction> getRestrictionType() {
        return RoadSectionRestriction.class;
    }
}
