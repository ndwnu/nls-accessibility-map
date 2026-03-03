package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.shapes.GHPoint;
import io.micrometer.core.annotation.Timed;
import java.util.ArrayList;
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
public class QueryGraphConfigurer {

    private static final double TOLERANCE_METRE_PRECISION = 0.00001;

    private static final boolean INCLUDE_ELEVATION = false;

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Timed(value = "accessibilitymap.queryGraph.createEdgeRestrictions")
    public Map<Integer, List<Restriction>> createEdgeRestrictions(
            EncodingManager encodingManager,
            QueryGraph queryGraph,
            List<SnapRestriction> snapRestrictions) {

        Map<Integer, List<Restriction>> edgeRestrictions = new HashMap<>();
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        Restrictions assignedRestrictions = new Restrictions();
        Stopwatch stopwatch = Stopwatch.createStarted();

        log.debug("Configuring query graph total nodes {} total edges {}", queryGraph.getNodes(), queryGraph.getEdges());
        snapRestrictions.forEach(snapRestriction -> {
            Restriction restriction = snapRestriction.restriction();
            Snap snap = snapRestriction.snap();
            // By creating a query graph with a snap, the closestNode of the snap is updated to a virtual node if applicable.
            // See QueryOverlayBuilder.buildVirtualEdges
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(snap.getClosestNode());

            while (edgeIterator.next()) {
                if (restrictionMatchesEdge(restriction, edgeIterator, encodingManager, snap)) {
                    edgeRestrictions.computeIfAbsent(edgeIterator.getEdgeKey(), integer -> new ArrayList<>());
                    edgeRestrictions.get(edgeIterator.getEdgeKey()).add(restriction);
                    assignedRestrictions.add(restriction);
                }
            }
        });

        Restrictions original = new Restrictions(snapRestrictions.stream().map(SnapRestriction::restriction).collect(Collectors.toSet()));
        Restrictions notAssigned = new Restrictions(Sets.difference(original, assignedRestrictions));

        log.atLevel(notAssigned.isEmpty() ? Level.INFO : Level.WARN)
                .setMessage(
                        "Query graph configuration summary. "
                        + "Total restrictions: {}. "
                        + "Total not assignable restrictions: {}. {}")
                .addArgument(snapRestrictions.size())
                .addArgument(notAssigned.size())
                .addArgument(notAssigned)
                .log();
        log.debug("Configured query graph in {} ms", stopwatch.elapsed().toMillis());
        return edgeRestrictions;
    }

    private boolean restrictionMatchesEdge(Restriction restriction, EdgeIterator edgeIterator, EncodingManager encodingManager, Snap snap) {
        return hasDirectionInSameDirectionAsCurrentEdge(edgeIterator, restriction)
               && matchesRestriction(encodingManager, edgeIterator, restriction)
               && isSnapInFrontOfEdge(edgeIterator, snap);
    }

    private static boolean matchesRestriction(EncodingManager encodingManager, EdgeIterator edgeIterator, Restriction restriction) {
        return edgeIterator.get(encodingManager.getIntEncodedValue(WAY_ID_KEY)) == restriction.roadSectionId();
    }

    private static boolean isSnapInFrontOfEdge(EdgeIteratorState edgeIteratorState, Snap snap) {

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
