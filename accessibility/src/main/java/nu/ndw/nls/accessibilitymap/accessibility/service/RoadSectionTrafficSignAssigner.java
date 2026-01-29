package nu.ndw.nls.accessibilitymap.accessibility.service;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoadSectionTrafficSignAssigner {

    public RoadSection assignRestriction(RoadSection roadSection, Map<Integer, List<Restriction>> restrictionsByEdgeKey) {
        roadSection.getRoadSectionFragments()
                .forEach(roadSectionFragment -> roadSectionFragment.getSegments()
                        .forEach(directionalSegment -> directionalSegment.setRestrictions(
                                restrictionsByEdgeKey.get(directionalSegment.getId()))));
        return roadSection;
    }
}
