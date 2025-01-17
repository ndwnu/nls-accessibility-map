package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAFFIC_SIGN_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.google.common.collect.Sets;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.shapes.GHPoint;
import io.micrometer.core.annotation.Timed;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.dto.EdgeAttribute;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.mappers.TrafficSignToEdgeAttributeMapper;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.TrafficSignSnap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Timed
public class QueryGraphConfigurer {

    private static final double TOLERANCE_METRE_PRECISION = 0.00001;

    private static final boolean INCLUDE_ELEVATION = false;

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    private final EdgeManager edgeManager;

    private final TrafficSignToEdgeAttributeMapper trafficSignToEdgeAttributeMapper;

    private final EncodingManager encodingManager;

    /**
     * This method iterates over all edges in both directions and determines whether the edge has a traffic sign that affects its access
     * forbidden attribute. If that is the case, it will assign the traffic sign to this edge and set its access forbidden attribute to
     * true.
     *
     * @param queryGraph          the queryGraph to configure
     * @param snappedTrafficSigns the list of snapped traffic signs that need to be assigned to edges.
     */
    public void configure(QueryGraph queryGraph, List<TrafficSignSnap> snappedTrafficSigns) {

        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        Set<TrafficSignSnap> assignedTrafficSignSnaps = new HashSet<>();
        log.debug("Configuring query graph total nodes {} total edges {}", queryGraph.getNodes(),
                queryGraph.getEdges());
        snappedTrafficSigns.forEach(trafficSignSnap -> {
            // By creating a query graph with a snap, the closestNode of the snap is updated to a virtual node if applicable.
            // See QueryOverlayBuilder.buildVirtualEdges
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(trafficSignSnap.getSnap().getClosestNode());
            while (edgeIterator.next()) {
                if (isTrafficSignInSameDirectionAsEdge(edgeIterator, trafficSignSnap) && isTrafficSignInFrontOfEdge(edgeIterator,
                        trafficSignSnap)) {
                    if (!trafficSignMatchesEdge(trafficSignSnap.getTrafficSign(), edgeIterator)) {
                        log.warn("Traffic sign {} and road section id {} does not match linked edge with road section id {}",
                                trafficSignSnap,
                                trafficSignSnap.getTrafficSign().roadSectionId(), getLinkId(edgeIterator));
                    } else {
                        assignTrafficSignIdToEdge(edgeIterator, trafficSignSnap.getTrafficSign().id());
                        applyTrafficSingRestrictionsToEdge(edgeIterator, trafficSignSnap.getTrafficSign());
                        assignedTrafficSignSnaps.add(trafficSignSnap);
                    }
                }
            }
        });

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

    private void assignTrafficSignIdToEdge(EdgeIterator edgeIterator, Integer trafficSignId) {

        edgeManager.setValueOnEdge(edgeIterator, TRAFFIC_SIGN_ID, trafficSignId);
    }

    private void applyTrafficSingRestrictionsToEdge(EdgeIterator edgeIterator, TrafficSign trafficSign) {

        EdgeAttribute edgeAttribute = trafficSignToEdgeAttributeMapper.mapToEdgeAttribute(trafficSign);
        edgeManager.setValueOnEdge(edgeIterator, edgeAttribute.key(), edgeAttribute.value());
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

    private boolean trafficSignMatchesEdge(TrafficSign trafficSign, EdgeIteratorState edgeIteratorState) {

        return getLinkId(edgeIteratorState) == trafficSign.roadSectionId();
    }

    private int getLinkId(EdgeIteratorState edge) {

        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
