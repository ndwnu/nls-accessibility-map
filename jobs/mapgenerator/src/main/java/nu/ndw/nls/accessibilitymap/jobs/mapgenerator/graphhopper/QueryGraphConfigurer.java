package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAFFIC_SIGN_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.google.common.collect.Sets;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.shapes.GHPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryGraphConfigurer {

    private static final double TOLERANCE_METRE_PRECISION = 0.00001;
    private static final boolean INCLUDE_ELEVATION = false;
    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
    private final EncodingManager encodingManager;

    /**
     * This method iterates over all edges in both directions and determines whether the edge has a traffic sign that
     * affects its access forbidden attribute. If that is the case, it will assign the traffic sign to this edge and set
     * its access forbidden attribute to true.
     *
     * @param queryGraph          the queryGraph to configure
     * @param snappedTrafficSigns the list of snapped traffic signs that need to be assigned to edges.
     */
    public void configure(QueryGraph queryGraph, List<TrafficSignSnap> snappedTrafficSigns) {
        Map<Integer, List<TrafficSignSnap>> trafficSignSnapsByRoadSection = snappedTrafficSigns
                .stream()
                .collect(groupingBy(additionalSnap -> additionalSnap.getTrafficSign().roadSectionId()));
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        int totalNodes = queryGraph.getNodes();
        List<EdgeIteratorState> edgesWithTrafficSigns = new ArrayList<>();
        Set<TrafficSignSnap> original = new HashSet<>(snappedTrafficSigns);
        Set<TrafficSignSnap> assigned = new HashSet<>();
        log.debug("Configuring query graph total nodes {} total edges {}", totalNodes, queryGraph.getEdges());
        for (int startNode = 0; startNode < queryGraph.getNodes(); startNode++) {
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(startNode);
            while (edgeIterator.next()) {
                boolean directionReversed = edgeIteratorStateReverseExtractor.hasReversed(edgeIterator);
                unblockEdge(edgeIterator);
                int linkId = getLinkId(edgeIterator);
                if (edgeHasTrafficSigns(linkId, trafficSignSnapsByRoadSection)) {
                    List<TrafficSignSnap> trafficSignSnaps = getTrafficSignSnapsFilteredOnDirection(
                            directionReversed, trafficSignSnapsByRoadSection, linkId);
                    assignTrafficSignsToEdge(trafficSignSnaps, edgeIterator, edgesWithTrafficSigns, directionReversed,
                            assigned);
                }
            }
        }
        Set<TrafficSignSnap> notAssigned = Sets.difference(original, assigned);
        Map<Integer, List<TrafficSignSnap>> notAssignedByRoadSectionId = notAssigned.stream()
                .collect(groupingBy(s -> s.getTrafficSign().roadSectionId()));
        log.info(
                "Query graph configuration summary. "
                + "Total traffic signs in request {} "
                + "Total edges assigned with traffic sign {}, "
                + "Total not assignable with traffic sign {}, "
                + "AssignedEdges {} ,"
                + "notAssigned {}",
                snappedTrafficSigns.size(),
                edgesWithTrafficSigns.size(),
                notAssignedByRoadSectionId.size(),
                edgesWithTrafficSigns, notAssignedByRoadSectionId);
    }

    private void assignTrafficSignsToEdge(List<TrafficSignSnap> trafficSignSnaps, EdgeIterator edgeIterator,
            List<EdgeIteratorState> edgesWithTrafficSigns, boolean directionReversed,
            Set<TrafficSignSnap> assignedTrafficSigns) {
        for (TrafficSignSnap trafficSignSnap : trafficSignSnaps) {
            if (isEdgeBehindTrafficSign(trafficSignSnap, edgeIterator)) {
                edgesWithTrafficSigns.add(edgeIterator);
                assignedTrafficSigns.add(trafficSignSnap);
                assignTrafficSignToEdge(edgeIterator, directionReversed, trafficSignSnap);
                applyRestrictionToEdge(edgeIterator, trafficSignSnap);
            }
        }
    }

    private void assignTrafficSignToEdge(EdgeIterator edgeIterator, boolean directionReversed,
            TrafficSignSnap trafficSignSnap) {
        IntEncodedValue intEncodedValue = encodingManager.getIntEncodedValue(TRAFFIC_SIGN_ID);
        if (directionReversed) {
            edgeIterator.setReverse(intEncodedValue, trafficSignSnap.getTrafficSign().id());
        } else {
            edgeIterator.set(intEncodedValue, trafficSignSnap.getTrafficSign().id());
        }
    }

    private void applyRestrictionToEdge(EdgeIterator edgeIterator,
            TrafficSignSnap trafficSignSnap) {
        String key = WindowTimeEncodedValue
                .valueOf(trafficSignSnap.getTrafficSign().trafficSignType().name())
                .getEncodedValue();
        BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
        edgeIterator.set(booleanEncodedValue, true);

    }

    @NotNull
    private static List<TrafficSignSnap> getTrafficSignSnapsFilteredOnDirection(boolean directionReversed,
            Map<Integer, List<TrafficSignSnap>> trafficSignSnapsByRoadSection, int linkId) {

        Predicate<TrafficSignSnap> filterOnDirection = getFilterOnDirection(directionReversed);
        return trafficSignSnapsByRoadSection
                .get(linkId)
                .stream()
                .filter(filterOnDirection)
                .toList();
    }


    private static Predicate<TrafficSignSnap> getFilterOnDirection(boolean directionReversed) {
        return directionReversed
                ? snap -> snap.getTrafficSign().direction().isBackward()
                : snap -> snap.getTrafficSign().direction().isForward();
    }

    private void unblockEdge(EdgeIterator edgeIterator) {
        Arrays.stream(WindowTimeEncodedValue.values())
                .map(WindowTimeEncodedValue::getEncodedValue).toList()
                .forEach(key ->
                {
                    BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
                    edgeIterator.set(booleanEncodedValue, false);
                });
    }

    private boolean edgeHasTrafficSigns(int linkId, Map<Integer, List<TrafficSignSnap>> additionalSnapByRoadSectionId) {
        return additionalSnapByRoadSectionId.containsKey(linkId);
    }

    private int getLinkId(EdgeIteratorState edge) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }

    private boolean isEdgeBehindTrafficSign(
            TrafficSignSnap additionalSnap,
            EdgeIteratorState edgeIteratorState) {

        GHPoint point = additionalSnap.getSnap().getSnappedPoint();
        Coordinate snapCoordinate = new Coordinate(point.getLon(), point.getLat());
        Coordinate edgeCoordinate = getEdgeStartCoordinate(edgeIteratorState);
        return edgeCoordinate.equals2D(snapCoordinate, TOLERANCE_METRE_PRECISION);
    }

    private static Coordinate getEdgeStartCoordinate(EdgeIteratorState edgeIteratorState) {
        LineString lineString = edgeIteratorState.fetchWayGeometry(FetchMode.ALL)
                .toLineString(INCLUDE_ELEVATION);
        return lineString.getStartPoint().getCoordinate();
    }

}
