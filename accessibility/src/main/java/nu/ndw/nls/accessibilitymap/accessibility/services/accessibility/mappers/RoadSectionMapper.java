package nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.mappers;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoadSectionMapper {

    @SuppressWarnings({"java:S5612", "java:S1941"})
    public @Valid Collection<RoadSection> mapToRoadSections(
            Iterable<IsochroneMatch> isochroneMatches,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey) {

        SortedMap<Integer, RoadSection> roadSectionsById = new TreeMap<>();
        SortedMap<Integer, RoadSectionFragment> roadSectionFragmentById = new TreeMap<>();

        isochroneMatches.forEach(isochroneMatch -> {
            int roadSectionId = isochroneMatch.getMatchedLinkId();
            int roadSectionFragmentId = isochroneMatch.getEdge().getEdge();
            int directionalSegmentId = isochroneMatch.getEdge().getEdgeKey();

            RoadSection roadSection = roadSectionsById.computeIfAbsent(
                    roadSectionId,
                    id -> RoadSection.builder()
                            .id(Long.valueOf(id))
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

            addSegmentsToRoadSectionFragment(
                    roadSectionFragment,
                    isochroneMatch,
                    getTrafficSigns(directionalSegmentId, trafficSignsByEdgeKey),
                    directionalSegmentId,
                    roadSectionFragmentById);
        });

        return roadSectionsById.values();
    }

    private static List<TrafficSign> getTrafficSigns(
            Integer directionalSegmentId,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey
    ) {
        if (trafficSignsByEdgeKey.containsKey(directionalSegmentId)) {
            return trafficSignsByEdgeKey.get(directionalSegmentId);
        }
        return Collections.emptyList();
    }

    private static void addSegmentsToRoadSectionFragment(
            RoadSectionFragment roadSectionFragment,
            IsochroneMatch isochroneMatch,
            List<TrafficSign> trafficSigns,
            int directionalSegmentId,
            SortedMap<Integer, RoadSectionFragment> roadSectionFragmentById) {

        if (isochroneMatch.isReversed()) {
            roadSectionFragment.setBackwardSegment(
                    buildDirectionalSegment(
                            directionalSegmentId,
                            Direction.BACKWARD,
                            isochroneMatch.getGeometry(),
                            roadSectionFragmentById.get(roadSectionFragment.getId()),
                            trafficSigns));
        } else {
            roadSectionFragment.setForwardSegment(
                    buildDirectionalSegment(
                            directionalSegmentId,
                            Direction.FORWARD,
                            isochroneMatch.getGeometry(),
                            roadSectionFragmentById.get(roadSectionFragment.getId()),
                            trafficSigns));
        }
    }

    private static DirectionalSegment buildDirectionalSegment(
            Integer id,
            Direction direction,
            LineString geometry,
            RoadSectionFragment roadSectionFragment,
            List<TrafficSign> trafficSign) {

        return DirectionalSegment.builder()
                .id(id)
                .direction(direction)
                .accessible(true)
                .lineString(geometry)
                .roadSectionFragment(roadSectionFragment)
                .trafficSigns(trafficSign)
                .build();
    }
}
