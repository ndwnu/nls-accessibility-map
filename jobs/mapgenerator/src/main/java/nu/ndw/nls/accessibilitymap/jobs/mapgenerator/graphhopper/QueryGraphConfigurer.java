package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAFFIC_SIGN_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AdditionalSnap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryGraphConfigurer {

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
    private final EncodingManager encodingManager;

    public void configure(QueryGraph queryGraph, List<AdditionalSnap> snappedTrafficSigns) {
        Map<Integer, List<AdditionalSnap>> additionalSnapByRoadSectionId = snappedTrafficSigns
                .stream()
                .collect(groupingBy(additionalSnap -> additionalSnap.getTrafficSign().roadSectionId()));
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();

        for (int startNode = 0; startNode < queryGraph.getNodes(); startNode++) {
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(startNode);
            while (edgeIterator.next()) {
                int linkId = getLinkId(edgeIterator);
                boolean directionReversed = edgeIteratorStateReverseExtractor.hasReversed(edgeIterator);
                if (edgeHasTrafficSigns(linkId, additionalSnapByRoadSectionId)) {
                    Predicate<AdditionalSnap> filterOnDirection = directionReversed
                            ? snap -> snap.getTrafficSign().direction().isBackward()
                            : snap -> snap.getTrafficSign().direction().isForward();
                    List<AdditionalSnap> additionalSnaps = additionalSnapByRoadSectionId
                            .get(linkId)
                            .stream()
                            .filter(filterOnDirection)
                            .toList();

                    log.debug("Found traffic signs {}", additionalSnaps);
                    for (AdditionalSnap additionalSnap : additionalSnaps) {
                        if (isEdgeBehindTrafficSign(additionalSnap, edgeIterator) && isBlocked(
                                additionalSnap, edgeIterator)) {
                            log.debug("Assigning traffic-sign id to  edge {} ", edgeIterator);
                            IntEncodedValue intEncodedValue = encodingManager.getIntEncodedValue(TRAFFIC_SIGN_ID);
                            if (directionReversed) {
                                edgeIterator.setReverse(intEncodedValue, additionalSnap.getTrafficSign().id());
                            } else {
                                edgeIterator.set(intEncodedValue, additionalSnap.getTrafficSign().id());
                            }
                        }

                        if (isEdgeBeforeTrafficSign(additionalSnap, edgeIterator) && isBlocked(
                                additionalSnap, edgeIterator)) {
                            String key = WindowTimeEncodedValue
                                    .valueOf(additionalSnap.getTrafficSign().trafficSignType().name())
                                    .getEncodedValue();
                            BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
                            log.info("Unblocking edge {} ", edgeIterator);
                            edgeIterator.set(booleanEncodedValue, false);
                        }
                    }
                }
            }
        }
    }

    private boolean isBlocked(AdditionalSnap additionalSnap, EdgeIterator edgeIterator) {
        String key = WindowTimeEncodedValue
                .valueOf(additionalSnap.getTrafficSign().trafficSignType().name())
                .getEncodedValue();
        BooleanEncodedValue booleanEncodedValue = encodingManager.getBooleanEncodedValue(key);
        boolean blocked = edgeIterator.get(booleanEncodedValue);
        log.info("Blocked edge {} {}", edgeIterator, blocked);
        return blocked;
    }

    private boolean isEdgeBeforeTrafficSign(AdditionalSnap additionalSnap, EdgeIterator edgeIterator) {
        GHPoint point = additionalSnap.getSnap().getSnappedPoint();
        Coordinate snapCoordinate = new Coordinate(point.lon, point.lat);
        Coordinate edgeCoordinate = getEdgeCoordinateBefore(edgeIterator);
        return edgeCoordinate.equals2D(snapCoordinate, 0.00001);
    }

    private boolean edgeHasTrafficSigns(int linkId, Map<Integer, List<AdditionalSnap>> additionalSnapByRoadSectionId) {
        return additionalSnapByRoadSectionId.containsKey(linkId);
    }

    private int getLinkId(EdgeIteratorState edge) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }

    private boolean isEdgeBehindTrafficSign(
            AdditionalSnap additionalSnap,
            EdgeIteratorState edgeIteratorState) {

        GHPoint point = additionalSnap.getSnap().getSnappedPoint();
        Coordinate snapCoordinate = new Coordinate(point.lon, point.lat);
        Coordinate edgeCoordinate = getEdgeCoordinateBehind(edgeIteratorState);

        return edgeCoordinate.equals2D(snapCoordinate, 0.00001);
    }

    private static Coordinate getEdgeCoordinateBefore(EdgeIteratorState edgeIteratorState) {
        LineString lineString = edgeIteratorState.fetchWayGeometry(FetchMode.ALL).toLineString(false);
        return lineString.getEndPoint().getCoordinate();
    }

    private static Coordinate getEdgeCoordinateBehind(EdgeIteratorState edgeIteratorState) {
        LineString lineString = edgeIteratorState.fetchWayGeometry(FetchMode.ALL).toLineString(false);
        return lineString.getStartPoint().getCoordinate();
    }

}
