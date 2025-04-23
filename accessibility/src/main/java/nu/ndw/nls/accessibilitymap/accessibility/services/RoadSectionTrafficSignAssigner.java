package nu.ndw.nls.accessibilitymap.accessibility.services;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoadSectionTrafficSignAssigner {

    public RoadSection assignTrafficSigns(RoadSection baseAccessibleRoad,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey) {
        baseAccessibleRoad.getRoadSectionFragments()
                .forEach(roadSectionFragment -> roadSectionFragment.getSegments()
                        .forEach(directionalSegment -> directionalSegment.setTrafficSigns(
                                trafficSignsByEdgeKey.get(directionalSegment.getId()))));
        return baseAccessibleRoad;
    }
}
