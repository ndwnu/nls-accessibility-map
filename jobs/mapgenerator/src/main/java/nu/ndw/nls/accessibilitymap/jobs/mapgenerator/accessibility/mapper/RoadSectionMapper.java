package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mapper;

import com.graphhopper.routing.ev.IntEncodedValue;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoadSectionMapper {

    private final NetworkGraphHopper networkGraphHopper;

    public Collection<RoadSection> mapToRoadSections(
            List<IsochroneMatch> isochroneMatches,
            Map<Integer, TrafficSign> trafficSignById) {

        IntEncodedValue trafficSignEncodedValueAttribute = networkGraphHopper.getEncodingManager()
                .getIntEncodedValue(AccessibilityLink.TRAFFIC_SIGN_ID);
        SortedMap<Integer, RoadSection> roadSectionsById = new TreeMap<>();
        SortedMap<Integer, RoadSectionFragment> roadSectionFragmentById = new TreeMap<>();

        isochroneMatches.forEach(isochroneMatch -> {
            int roadSectionId = isochroneMatch.getMatchedLinkId();
            int roadSectionFragmentId = isochroneMatch.getEdge().getEdge();
            int directionalSegmentId = isochroneMatch.getEdge().getEdgeKey();

            RoadSection roadSection = roadSectionsById.computeIfAbsent(
                    roadSectionId,
                    id -> RoadSection.builder()
                            .id(id)
                            .build());

            RoadSectionFragment roadSectionFragment = roadSectionFragmentById.computeIfAbsent(
                    roadSectionFragmentId,
                    id -> {
                        RoadSectionFragment roadSectionFragmentNew = RoadSectionFragment.builder()
                                .id(id)
                                .roadSection(roadSection)
                                .build();

                        roadSection.getRoadSectionFragments().add(roadSectionFragmentNew);
                        return roadSectionFragmentNew;
                    });

            if (isochroneMatch.isReversed()) {
                roadSectionFragment.setBackwardSegment(
                        buildDirectionalSegment(
                                directionalSegmentId,
                                Direction.BACKWARD,
                                isochroneMatch.getGeometry(),
                                roadSectionFragmentById.get(roadSectionFragmentId),
                                trafficSignById.get(isochroneMatch.getEdge().get(trafficSignEncodedValueAttribute))));
            } else {
                roadSectionFragment.setForwardSegment(
                        buildDirectionalSegment(
                                directionalSegmentId,
                                Direction.FORWARD,
                                isochroneMatch.getGeometry(),
                                roadSectionFragmentById.get(roadSectionFragmentId),
                                trafficSignById.get(isochroneMatch.getEdge().get(trafficSignEncodedValueAttribute))));
            }
        });

        return roadSectionsById.values();
    }

    private DirectionalSegment buildDirectionalSegment(
            Integer id,
            Direction direction,
            LineString geometry,
            RoadSectionFragment roadSectionFragment,
            TrafficSign trafficSign) {

        return DirectionalSegment.builder()
                .id(id)
                .direction(direction)
                .accessible(true)
                .lineString(geometry)
                .roadSectionFragment(roadSectionFragment)
                .trafficSign(trafficSign)
                .build();
    }
}
