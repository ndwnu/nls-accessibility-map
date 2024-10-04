package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoadSectionMapper {

    public Collection<RoadSection> mapToRoadSections(
            List<IsochroneMatch> isochroneMatches,
            Map<Integer, TrafficSign> trafficSignById) {

        SortedMap<Integer, RoadSection> roadSectionsById = new TreeMap<>();
        SortedMap<Integer, RoadSectionFragment> roadSectionFragmentById = new TreeMap<>();

        isochroneMatches.forEach(isochroneMatch -> {
            int roadSectionId = isochroneMatch.getMatchedLinkId();
            int roadSectionFragmentId = isochroneMatch.getEdgeId();
            int directionalSegmentId = isochroneMatch.getEdgeKey();

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
                roadSectionFragment.getBackwardSegments().add(
                        buildDirectionalSegment(
                                directionalSegmentId,
                                Direction.BACKWARD,
                                isochroneMatch.getGeometry(),
                                roadSectionFragmentById.get(roadSectionFragmentId),
                                trafficSignById.get(directionalSegmentId))); // TODO: replace with encodedValue
            } else {
                roadSectionFragment.getForwardSegments().add(
                        buildDirectionalSegment(
                                directionalSegmentId,
                                Direction.FORWARD,
                                isochroneMatch.getGeometry(),
                                roadSectionFragmentById.get(roadSectionFragmentId),
                                trafficSignById.get(directionalSegmentId)));  // TODO: replace with encodedValue
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
