package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.shapes.GHPoint;
import io.micrometer.core.annotation.Timed;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.SnapRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
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

    public Map<Integer, Restriction> createEdgeRestrictions(QueryGraph queryGraph, List<SnapRestriction> snaps) {

        Map<Integer, Restriction> edgeRestrictions = new HashMap<>();
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        Restrictions assignedRestrictions = new Restrictions();
        Stopwatch stopwatch = Stopwatch.createStarted();

        log.debug("Configuring query graph total nodes {} total edges {}", queryGraph.getNodes(), queryGraph.getEdges());
        snaps.forEach(snapRestriction -> {
            Restriction restriction = snapRestriction.restriction();
            Snap snap = snapRestriction.snap();
            // By creating a query graph with a snap, the closestNode of the snap is updated to a virtual node if applicable.
            // See QueryOverlayBuilder.buildVirtualEdges
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(snap.getClosestNode());

            while (edgeIterator.next()) {
                if (hasDirectionInSameDirectionAsCurrentEdge(edgeIterator, restriction)
                    && isTrafficSignInFrontOfEdge(edgeIterator, snap)) {
                    edgeRestrictions.put(edgeIterator.getEdgeKey(), restriction);
                    assignedRestrictions.add(restriction);
                }
            }
        });

        Restrictions original = new Restrictions(snaps.stream().map(SnapRestriction::restriction).collect(Collectors.toSet()));
        Restrictions notAssigned = new Restrictions(Sets.difference(original, assignedRestrictions));

        log.atLevel(notAssigned.isEmpty() ? Level.INFO : Level.WARN)
                .setMessage(
                        "Query graph configuration summary. "
                        + "Total restriction: {}. "
                        + "Total not assignable restrictions: {}. {}")
                .addArgument(snaps.size())
                .addArgument(notAssigned.size())
                .addArgument(notAssigned)
                .log();
        log.debug("Configured query graph in {} ms", stopwatch.elapsed().toMillis());
        return edgeRestrictions;
    }

    private static boolean isTrafficSignInFrontOfEdge(EdgeIteratorState edgeIteratorState, Snap snap) {

        GHPoint point = snap.getSnappedPoint();
        Coordinate snapCoordinate = new Coordinate(point.getLon(), point.getLat());
        Coordinate edgeCoordinate = getEdgeStartCoordinate(edgeIteratorState);

        return edgeCoordinate.equals2D(snapCoordinate, TOLERANCE_METRE_PRECISION);
    }

    private boolean hasDirectionInSameDirectionAsCurrentEdge(EdgeIterator edgeIterator, Restriction restriction) {

        if (edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)) {
            return restriction.direction().isBackward();
        }

        return restriction.direction().isForward();
    }

    private static Coordinate getEdgeStartCoordinate(EdgeIteratorState edgeIteratorState) {

        LineString lineString = edgeIteratorState
                .fetchWayGeometry(FetchMode.ALL)
                .toLineString(INCLUDE_ELEVATION);
        return lineString.getStartPoint().getCoordinate();
    }
}
