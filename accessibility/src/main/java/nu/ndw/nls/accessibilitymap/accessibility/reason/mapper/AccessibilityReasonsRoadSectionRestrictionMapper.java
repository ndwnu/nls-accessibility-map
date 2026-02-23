package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection.RoadSectionRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibleReason;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsRoadSectionRestrictionMapper implements RestrictionMapper {

    public List<AccessibilityReason<?>> mapRestrictions(Restrictions restrictions) {

        return restrictions.stream()
                .filter(RoadSectionRestriction.class::isInstance)
                .map(RoadSectionRestriction.class::cast)
                .flatMap(roadSectionRestriction ->
                        Stream.of(mapAccessibility(roadSectionRestriction))
                                .flatMap(List::stream))
                .toList();
    }

    private List<AccessibilityReason<?>> mapAccessibility(RoadSectionRestriction roadSectionRestriction) {
        return List.of(AccessibleReason.builder()
                .value(!roadSectionRestriction.isRestrictive(null))
                .restrictions(Set.of(roadSectionRestriction))
                .build());
    }
}
