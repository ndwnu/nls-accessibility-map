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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.event.Level;
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

        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();

        Set<TrafficSignSnap> assignedTrafficSignSnaps = new HashSet<>();
        Map<Integer, List<TrafficSignSnap>> trafficSignSnapsByRoadSectionId = snappedTrafficSigns
                .stream()
                .collect(groupingBy(additionalSnap -> additionalSnap.getTrafficSign().roadSectionId()));

        log.debug("Configuring query graph total nodes {} total edges {}", queryGraph.getNodes(),
                queryGraph.getEdges());
        for (int startNode = 0; startNode < queryGraph.getNodes(); startNode++) {
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(startNode);
            while (edgeIterator.next()) {
                unblockEdge(edgeIterator);

                int roadSectionId = edgeIterator.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));

                trafficSignSnapsByRoadSectionId.getOrDefault(roadSectionId, List.of()).stream()
                        .filter(trafficSignSnap -> isTrafficSignInSameDirectionAsEdge(edgeIterator, trafficSignSnap))
                        .filter(trafficSignSnap -> isTrafficSignInFrontOfEdge(edgeIterator, trafficSignSnap))
                        .forEach(trafficSignSnap -> {
                            assignTrafficSignIdToEdge(edgeIterator, trafficSignSnap.getTrafficSign().id());
                            blockEdgeWithTrafficSignRestrictions(edgeIterator, trafficSignSnap.getTrafficSign());

                            assignedTrafficSignSnaps.add(trafficSignSnap);
                        });
            }
        }

        printEdges(queryGraph, edgeExplorer);

        Set<TrafficSignSnap> original = new HashSet<>(snappedTrafficSigns);
        Set<TrafficSignSnap> notAssigned = Sets.difference(original, assignedTrafficSignSnaps);
        Map<Integer, List<TrafficSignSnap>> notAssignedByRoadSectionId = notAssigned.stream()
                .collect(groupingBy(s -> s.getTrafficSign().roadSectionId()));

        log.atLevel(notAssignedByRoadSectionId.isEmpty() ? Level.INFO : Level.WARN)
                .setMessage(
                        "Query graph configuration summary. "
                                + "Total traffic signs in request {}. "
                                + "Total not assignable road sections with traffic sign {}, notAssigned {}")
                .addArgument(snappedTrafficSigns.size())
                .addArgument(notAssignedByRoadSectionId.size())
                .addArgument(notAssignedByRoadSectionId)
                .log();
    }

    private void printEdges(QueryGraph queryGraph, EdgeExplorer edgeExplorer) {

        Map<Integer, List<String>> infoByRoadSectionId = new HashMap<>();
        for (int startNode = 0; startNode < queryGraph.getNodes(); startNode++) {
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(startNode);
            while (edgeIterator.next()) {
                int roadSectionId = edgeIterator.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Edge with id: %s and key: %s".formatted(edgeIterator.getEdge(), edgeIterator.getEdgeKey()));
                stringBuilder.append("  Properties:");
                Arrays.stream(WindowTimeEncodedValue.values())
                        .map(WindowTimeEncodedValue::getEncodedValue).toList()
                        .forEach(key ->
                        {
                            BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
                            edgeIterator.get(booleanEncodedValue);
                            edgeIterator.getReverse(booleanEncodedValue);

                            stringBuilder.append("  %s=%s (Reversed: %s)".formatted( key, edgeIterator.get(booleanEncodedValue), edgeIterator.getReverse(booleanEncodedValue)));
                        });

                List<String> info = infoByRoadSectionId.getOrDefault(roadSectionId, new ArrayList<>());
                info.add(stringBuilder.toString());
                infoByRoadSectionId.put(roadSectionId, info);
            }
        }

        infoByRoadSectionId.forEach((roadSectionId, info) -> {
            log.debug("---------------------------");
            log.debug("RoadSectionId: {}", roadSectionId);
            info.forEach(log::debug);
            log.debug("---------------------------");
        });
    }

    private void unblockEdge(EdgeIterator edgeIterator) {
        Arrays.stream(WindowTimeEncodedValue.values())
                .map(WindowTimeEncodedValue::getEncodedValue).toList()
                .forEach(key ->
                {
                    BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
                    edgeIterator.set(booleanEncodedValue, false);
                    edgeIterator.setReverse(booleanEncodedValue, false);
                });
    }

    private void assignTrafficSignIdToEdge(
            EdgeIterator edgeIterator,
            Integer trafficSignId) {

        IntEncodedValue intEncodedValue = encodingManager.getIntEncodedValue(TRAFFIC_SIGN_ID);
        if (edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)) {
            edgeIterator.setReverse(intEncodedValue, trafficSignId);
        } else {
            edgeIterator.set(intEncodedValue, trafficSignId);
        }
    }

    private void blockEdgeWithTrafficSignRestrictions(
            EdgeIterator edgeIterator,
            TrafficSign trafficSign) {

        String trafficSignAttributeKey = WindowTimeEncodedValue.valueOf(trafficSign.trafficSignType().name())
                .getEncodedValue();
        BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(trafficSignAttributeKey);
        edgeIterator.set(booleanEncodedValue, true);
    }

    private boolean isTrafficSignInFrontOfEdge(EdgeIteratorState edgeIteratorState, TrafficSignSnap trafficSignSnap) {

        GHPoint point = trafficSignSnap.getSnap().getSnappedPoint();
        Coordinate snapCoordinate = new Coordinate(point.getLon(), point.getLat());
        Coordinate edgeCoordinate = getEdgeStartCoordinate(edgeIteratorState);

        return edgeCoordinate.equals2D(snapCoordinate, TOLERANCE_METRE_PRECISION);
    }

    private boolean isTrafficSignInSameDirectionAsEdge(EdgeIterator edgeIterator, TrafficSignSnap trafficSignSnap) {

        if (edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)) {
            return trafficSignSnap.getTrafficSign().direction().isBackward();
        }

        return trafficSignSnap.getTrafficSign().direction().isForward();
    }

    private static Coordinate getEdgeStartCoordinate(EdgeIteratorState edgeIteratorState) {
        LineString lineString = edgeIteratorState
                .fetchWayGeometry(FetchMode.ALL)
                .toLineString(INCLUDE_ELEVATION);
        return lineString.getStartPoint().getCoordinate();
    }
}
