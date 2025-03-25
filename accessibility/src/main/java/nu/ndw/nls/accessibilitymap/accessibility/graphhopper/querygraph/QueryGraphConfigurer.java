package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.google.common.base.Stopwatch;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.TrafficSignEdgeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.TrafficSignEdgeRestrictions;
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

    private final EncodingManager encodingManager;

    /**
     * Creates a mapping of edge restrictions based on traffic signs and their snapped locations in a query graph. This method associates
     * traffic signs with appropriate edges in the graph considering the traffic sign's direction, placement, and compatibility with the
     * edge properties. It logs the configuration process and identifies any traffic signs that could not be assigned to an edge.
     *
     * @param queryGraph          the query graph containing the graph structure and edges to be examined.
     * @param snappedTrafficSigns a list of snapped traffic signs with location data used to determine their closest edges.
     * @return a TrafficSignEdgeRestrictions object containing a list of restrictions associating traffic signs with edges.
     */
    public TrafficSignEdgeRestrictions createEdgeRestrictions(QueryGraph queryGraph, List<TrafficSignSnap> snappedTrafficSigns) {
        List<TrafficSignEdgeRestriction> edgeRestrictions = new ArrayList<>();
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        Set<TrafficSignSnap> assignedTrafficSignSnaps = new HashSet<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.debug("Configuring query graph total nodes {} total edges {}", queryGraph.getNodes(),
                queryGraph.getEdges());
        snappedTrafficSigns.forEach(trafficSignSnap -> {
            // By creating a query graph with a snap, the closestNode of the snap is updated to a virtual node if applicable.
            // See QueryOverlayBuilder.buildVirtualEdges
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(trafficSignSnap.getSnap().getClosestNode());
            while (edgeIterator.next()) {
                if (isTrafficSignInSameDirectionAsEdge(edgeIterator, trafficSignSnap) && isTrafficSignInFrontOfEdge(edgeIterator,
                        trafficSignSnap)) {
                    if (trafficSignDoesNotMatchEdge(trafficSignSnap.getTrafficSign(), edgeIterator)) {
                        log.warn("Traffic sign {} and road section id {} does not match linked edge with road section id {}",
                                trafficSignSnap,
                                trafficSignSnap.getTrafficSign().roadSectionId(), getLinkId(edgeIterator));
                    } else {

                        edgeRestrictions.add(TrafficSignEdgeRestriction.builder()
                                .edgeKey(edgeIterator.getEdgeKey())
                                .trafficSignId(trafficSignSnap.getTrafficSign().id())
                                .build());
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
        log.debug("Configured query graph in {} ms", stopwatch.elapsed().toMillis());
        return new TrafficSignEdgeRestrictions(edgeRestrictions);
    }

    private static boolean isTrafficSignInFrontOfEdge(EdgeIteratorState edgeIteratorState, TrafficSignSnap trafficSignSnap) {

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

    private boolean trafficSignDoesNotMatchEdge(TrafficSign trafficSign, EdgeIteratorState edgeIteratorState) {

        return getLinkId(edgeIteratorState) != trafficSign.roadSectionId();
    }

    private int getLinkId(EdgeIteratorState edge) {

        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
