package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
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
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone.IsoLabelToGeometryMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoadSectionMapper {

    private final IsoLabelToGeometryMapper isoLabelToGeometryMapper;

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @SuppressWarnings({"java:S5612", "java:S1941"})
    @Timed(value = "accessibilitymap.roadSection.mapping")
    public @Valid Collection<RoadSection> map(
            AccessibilityNetwork accessibilityNetwork,
            List<IsoLabel> isoLabels,
            Map<Integer, List<Restriction>> restrictionsByEdgeKey
    ) {

        log.debug("Mapping iso labels to road sections");

        SortedMap<Integer, RoadSection> roadSectionsById = new TreeMap<>();
        SortedMap<Integer, RoadSectionFragment> roadSectionFragmentById = new TreeMap<>();

        isoLabels.forEach(isoLabel -> {
            EdgeIteratorState currentEdge = accessibilityNetwork.getQueryGraph().getEdgeIteratorState(
                    isoLabel.getEdge(),
                    isoLabel.getNode());
            LineString lineString = isoLabelToGeometryMapper.map(currentEdge);
            int roadSectionId = currentEdge.get(accessibilityNetwork.getNetworkData().getNetworkGraphHopper().getEncodingManager()
                    .getIntEncodedValue(WAY_ID_KEY));

            int roadSectionFragmentId = currentEdge.getEdge();
            int directionalSegmentId = currentEdge.getEdgeKey();

            RoadSection roadSection = roadSectionsById.computeIfAbsent(
                    roadSectionId,
                    id -> RoadSection.builder()
                            .id(Long.valueOf(id))
                            .functionalRoadClass(accessibilityNetwork.getNetworkData()
                                    .getNwbData()
                                    .findAccessibilityNwbRoadSectionById(id)
                                    .orElseThrow()
                                    .functionalRoadClass())
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

            boolean isReversed = edgeIteratorStateReverseExtractor.hasReversed(currentEdge);
            List<Restriction> restrictions = restrictionsByEdgeKey.getOrDefault(directionalSegmentId, List.of());
            addSegmentsToRoadSectionFragment(
                    roadSectionFragment,
                    isReversed,
                    lineString,
                    directionalSegmentId,
                    roadSectionFragmentById,
                    restrictions);
        });

        log.debug("Mapped {} iso labels to {} road sections", isoLabels.size(), roadSectionsById.size());
        return new ArrayList<>(roadSectionsById.values());
    }

    private static void addSegmentsToRoadSectionFragment(
            RoadSectionFragment roadSectionFragment,
            boolean isReversed,
            LineString lineString,
            int directionalSegmentId,
            SortedMap<Integer, RoadSectionFragment> roadSectionFragmentById,
            List<Restriction> restrictions
    ) {

        if (isReversed) {
            roadSectionFragment.setBackwardSegment(
                    buildDirectionalSegment(
                            directionalSegmentId,
                            Direction.BACKWARD,
                            lineString,
                            roadSectionFragmentById.get(roadSectionFragment.getId()),
                            restrictions));
        } else {
            roadSectionFragment.setForwardSegment(
                    buildDirectionalSegment(
                            directionalSegmentId,
                            Direction.FORWARD,
                            lineString,
                            roadSectionFragmentById.get(roadSectionFragment.getId()),
                            restrictions));
        }
    }

    private static DirectionalSegment buildDirectionalSegment(
            Integer id,
            Direction direction,
            LineString lineString,
            RoadSectionFragment roadSectionFragment,
            List<Restriction> restrictions
    ) {

        return DirectionalSegment.builder()
                .id(id)
                .direction(direction)
                .accessible(true)
                .lineString(lineString)
                .roadSectionFragment(roadSectionFragment)
                .restrictions(new Restrictions(restrictions))
                .build();
    }
}
