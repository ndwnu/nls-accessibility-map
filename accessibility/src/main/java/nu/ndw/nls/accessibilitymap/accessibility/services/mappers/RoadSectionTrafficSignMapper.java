package nu.ndw.nls.accessibilitymap.accessibility.services.mappers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
public class RoadSectionTrafficSignMapper {

    public Collection<RoadSection> mapTrafficSigns(Collection<RoadSection> baseAccessibleRoads,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey) {
        return baseAccessibleRoads.stream()
                .map(r -> r.toBuilder()
                        .roadSectionFragments(r.getRoadSectionFragments()
                                .stream()
                                .map(f -> f.toBuilder()
                                        .backwardSegment(mapBackwardSegment(trafficSignsByEdgeKey, f))
                                        .forwardSegment(mapForwardSegment(trafficSignsByEdgeKey, f))
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    private static DirectionalSegment mapForwardSegment(
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey, RoadSectionFragment roadSectionFragment) {
        return roadSectionFragment.hasForwardSegment() ? roadSectionFragment.getForwardSegment().toBuilder()
                .trafficSigns(trafficSignsByEdgeKey.getOrDefault(roadSectionFragment.getForwardSegment().getId(), null))
                .build() : null;
    }

    private static DirectionalSegment mapBackwardSegment(
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey, RoadSectionFragment roadSectionFragment) {
        return roadSectionFragment.hasBackwardSegment() ? roadSectionFragment.getBackwardSegment()
                .toBuilder()
                .trafficSigns(trafficSignsByEdgeKey.getOrDefault(roadSectionFragment.getBackwardSegment().getId(), null))
                .build() : null;
    }
}
