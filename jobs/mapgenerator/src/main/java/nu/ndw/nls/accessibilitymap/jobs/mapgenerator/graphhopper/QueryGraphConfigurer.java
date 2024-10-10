package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAFFIC_SIGN_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.google.common.collect.Sets;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryGraphConfigurer {

    public static final double TOLERANCE = 0.00001;
    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
    private final EncodingManager encodingManager;

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
                int linkId = getLinkId(edgeIterator);
                boolean directionReversed = edgeIteratorStateReverseExtractor.hasReversed(edgeIterator);
                unblockEdge(edgeIterator);
                if (edgeHasTrafficSigns(linkId, trafficSignSnapsByRoadSection)) {
                    Predicate<TrafficSignSnap> filterOnDirection = directionReversed
                            ? snap -> snap.getTrafficSign().direction().isBackward()
                            : snap -> snap.getTrafficSign().direction().isForward();

                    List<TrafficSignSnap> trafficSignSnaps = trafficSignSnapsByRoadSection
                            .get(linkId)
                            .stream()
                            .filter(filterOnDirection)
                            .toList();

                    for (TrafficSignSnap trafficSignSnap : trafficSignSnaps) {

                        if (isEdgeBehindTrafficSign(trafficSignSnap, edgeIterator)) {
                            edgesWithTrafficSigns.add(edgeIterator);
                            IntEncodedValue intEncodedValue = encodingManager.getIntEncodedValue(TRAFFIC_SIGN_ID);
                            if (directionReversed) {
                                edgeIterator.setReverse(intEncodedValue, trafficSignSnap.getTrafficSign().id());
                            } else {
                                edgeIterator.set(intEncodedValue, trafficSignSnap.getTrafficSign().id());
                            }
                            String key = WindowTimeEncodedValue
                                    .valueOf(trafficSignSnap.getTrafficSign().trafficSignType().name())
                                    .getEncodedValue();
                            BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
                            edgeIterator.set(booleanEncodedValue, true);
                            assigned.add(trafficSignSnap);
                        }

                    }
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
                        + "notAssigned {} ",
                snappedTrafficSigns.size(),
                edgesWithTrafficSigns.size(),
                notAssignedByRoadSectionId.size(),
                edgesWithTrafficSigns,notAssignedByRoadSectionId);
    }

    private boolean isBlocked(TrafficSignSnap additionalSnap, EdgeIterator edgeIterator) {
        String key = WindowTimeEncodedValue
                .valueOf(additionalSnap.getTrafficSign().trafficSignType().name())
                .getEncodedValue();
        BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
        boolean blocked = edgeIterator.get(booleanEncodedValue);
        return blocked;
    }

    private void unblockEdge(EdgeIterator edgeIterator) {
        Arrays.stream(WindowTimeEncodedValue.values())
                .map(e -> e.getEncodedValue()).toList()
                .forEach(key ->
                {
                    BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
                    edgeIterator.set(booleanEncodedValue, false);
                });
    }

    private boolean isEdgeBeforeTrafficSign(TrafficSignSnap additionalSnap, EdgeIterator edgeIterator) {
        GHPoint point = additionalSnap.getSnap().getSnappedPoint();
        Coordinate snapCoordinate = new Coordinate(point.lon, point.lat);
        Coordinate edgeCoordinate = getEdgeCoordinateBefore(edgeIterator);
        return edgeCoordinate.equals2D(snapCoordinate, TOLERANCE);
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
        Coordinate snapCoordinate = new Coordinate(point.lon, point.lat);
        Coordinate edgeCoordinate = getEdgeCoordinateBehind(edgeIteratorState);
        return edgeCoordinate.equals2D(snapCoordinate, TOLERANCE);
    }

    private static Coordinate getEdgeCoordinateBefore(EdgeIteratorState edgeIteratorState) {
        LineString lineString = edgeIteratorState.fetchWayGeometry(FetchMode.ALL).toLineString(false);
        return lineString.getEndPoint().getCoordinate();
    }

    private static Coordinate getEdgeCoordinateBehind(EdgeIteratorState edgeIteratorState) {
        LineString lineString = edgeIteratorState.fetchWayGeometry(FetchMode.ALL).toLineString(false);
        return lineString.getStartPoint().getCoordinate();
    }

    private int edgeKey(EdgeIteratorState edge, QueryGraph queryGraph) {
        if (queryGraph.isVirtualEdge(edge.getEdge())) {
            return ((VirtualEdgeIteratorState) edge).getOriginalEdgeKey();
        } else {
            return edge.getEdgeKey();
        }
    }

}
