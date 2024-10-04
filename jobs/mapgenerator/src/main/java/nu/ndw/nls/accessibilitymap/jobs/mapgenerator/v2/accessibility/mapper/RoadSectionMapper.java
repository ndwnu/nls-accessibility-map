package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.springframework.stereotype.Component;

@Component
public class RoadSectionMapper {

    public Collection<RoadSection> mapToRoadSections(
            List<IsochroneMatch> isochroneMatches,
            Map<Integer, TrafficSign> edgeStateByTrafficSignId) {

        SortedMap<Integer, RoadSection> roadSectionsById = new TreeMap<>();

        SortedMap<Integer, List<DirectionalSegment>> directionalSegmentsByRoadSectionFragmentId = new TreeMap<>();
        SortedMap<Integer, RoadSectionFragment> roadSectionFragmentById = new TreeMap<>();

        isochroneMatches.forEach(isochroneMatch -> {
            int roadSectionId = isochroneMatch.getMatchedLinkId();
            int roadSectionFragmentId = isochroneMatch.getEdgeId();

            RoadSection roadSection = roadSectionsById.computeIfAbsent(
                    roadSectionId,
                    id -> RoadSection.builder()
                            .id(id)
                            .build());

            List<DirectionalSegment> directionalSegments = directionalSegmentsByRoadSectionFragmentId.computeIfAbsent(
                    roadSectionFragmentId,
                    id -> new ArrayList<>());

            roadSectionFragmentById.computeIfAbsent(
                    roadSectionFragmentId,
                    id -> RoadSectionFragment.builder()
                            .id(id)
                            .roadSection(roadSection)
                            .build());

            if (isochroneMatch.isReversed()) {
                directionalSegments.add(
                        buildDirectionalSegment(
                                Direction.BACKWARD,
                                isochroneMatch,
                                roadSectionFragmentById.get(roadSectionFragmentId),
                                edgeStateByTrafficSignId.get(isochroneMatch.getEdgeKey())));
            } else {
                directionalSegments.add(
                        buildDirectionalSegment(
                                Direction.FORWARD,
                                isochroneMatch,
                                roadSectionFragmentById.get(roadSectionFragmentId),
                                edgeStateByTrafficSignId.get(isochroneMatch.getEdgeKey())));
            }
        });

        return roadSectionsById.values();
    }

    private DirectionalSegment buildDirectionalSegment(
            Direction direction,
            IsochroneMatch isochroneMatch,
            RoadSectionFragment roadSectionFragment,
            TrafficSign trafficSign) {

        return DirectionalSegment.builder()
                .id(isochroneMatch.getEdgeKey())
                .direction(direction)
                .accessible(true)
                .lineString(isochroneMatch.getGeometry())
                .roadSectionFragment(roadSectionFragment)
                .trafficSign(trafficSign)
                .build();
    }
}
