package nu.ndw.nls.accessibilitymap.accessibility.services.mappers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoadSectionAccessibilityMapper {

    public Collection<RoadSection> mapCombined(Collection<RoadSection> baseAccessibleRoads, Set<Integer> accessibleEdgesWithRestrictions,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey) {
        return baseAccessibleRoads.stream()
                .map(r -> r.toBuilder()
                        .roadSectionFragments(r.getRoadSectionFragments()
                                .stream()
                                .map(f -> f.toBuilder()
                                        .backwardSegment(mapBackwardSegment(accessibleEdgesWithRestrictions, trafficSignsByEdgeKey, f))
                                        .forwardSegment(mapForwardSegment(accessibleEdgesWithRestrictions, trafficSignsByEdgeKey, f))
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    private static DirectionalSegment mapForwardSegment(Set<Integer> accessibleEdgesWithRestrictions,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey, RoadSectionFragment roadSectionFragment) {
        return roadSectionFragment.hasForwardSegment() ? roadSectionFragment.getForwardSegment().toBuilder()
                .trafficSigns(trafficSignsByEdgeKey.getOrDefault(roadSectionFragment.getForwardSegment().getId(), null))
                .accessible(accessibleEdgesWithRestrictions.contains(roadSectionFragment.getForwardSegment().getId()))
                .build() : null;
    }

    private static DirectionalSegment mapBackwardSegment(Set<Integer> accessibleEdgesWithRestrictions,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey, RoadSectionFragment roadSectionFragment) {
        return roadSectionFragment.hasBackwardSegment() ? roadSectionFragment.getBackwardSegment()
                .toBuilder()
                .accessible(accessibleEdgesWithRestrictions.contains(roadSectionFragment.getBackwardSegment().getId()))
                .trafficSigns(trafficSignsByEdgeKey.getOrDefault(roadSectionFragment.getBackwardSegment().getId(), null))
                .build() : null;
    }
}
