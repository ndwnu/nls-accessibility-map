package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.weighting;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.shapes.GHPoint;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.AdditionalSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignDirection;
import org.locationtech.jts.geom.Coordinate;

@Slf4j
public class RestrictionWeightingAdapter implements Weighting {

    private final Weighting adaptedWeighting;

    private final Map<Integer, List<AdditionalSnap>> snappedTrafficSignsByRoadSection;

    private final EncodingManager encodingManager;

    public RestrictionWeightingAdapter(Weighting adaptedWeighting, List<AdditionalSnap> snappedTrafficSigns,
            EncodingManager encodingManager) {
        this.adaptedWeighting = adaptedWeighting;
        this.snappedTrafficSignsByRoadSection = snappedTrafficSigns
                .stream()
                .filter(a-> a.getTrafficSign()!=null)
                .collect(groupingBy(tr -> tr.getTrafficSign()
                        .roadSectionId()));
        this.encodingManager = encodingManager;
    }

    @Override
    public double calcMinWeightPerDistance() {
        return adaptedWeighting.calcMinWeightPerDistance();
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeIteratorState, boolean reverse) {
        if (edgeHasTrafficSigns(getLinkId(edgeIteratorState))) {
            Predicate<AdditionalSnap> filterOnDirection = reverse ? (snap) -> snap
                    .getTrafficSign()
                    .direction()
                    .isBackward()
                    : (snap) -> snap.getTrafficSign().direction().isForward();
            List<AdditionalSnap> additionalSnaps = snappedTrafficSignsByRoadSection
                    .get(getLinkId(edgeIteratorState))
                    .stream().filter(filterOnDirection)
                    .toList();
            log.info("Found traffic signs {}", additionalSnaps);
            for (AdditionalSnap additionalSnap : additionalSnaps) {
                if (isEdgeBehindTrafficSign(additionalSnap, edgeIteratorState, reverse)) {
                    log.info("Blocking access to edge {} {} {}", additionalSnap,edgeIteratorState,reverse);
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
        return adaptedWeighting.calcEdgeWeight(edgeIteratorState, reverse);
    }

    private boolean isEdgeBehindTrafficSign(AdditionalSnap additionalSnap, EdgeIteratorState edgeIteratorState,
            boolean reverse) {
        GHPoint point = additionalSnap.getSnap().getSnappedPoint();
        Coordinate snapCoordinate = new Coordinate(point.lon,
                point.lat);
        Coordinate edgeCoordinate = getEdgeCoordinate(additionalSnap, edgeIteratorState, reverse);
        return edgeCoordinate.equals2D(snapCoordinate, 0.00001);
    }

    private static Coordinate getEdgeCoordinate(AdditionalSnap additionalSnap, EdgeIteratorState edgeIteratorState,
            boolean reverse) {
        if (isBidirectionalAndReverse(additionalSnap, reverse)) {
            log.info("Bidirectional traffic sign on reverse edge {}", additionalSnap);
            return edgeIteratorState.fetchWayGeometry(FetchMode.ALL)
                    .toLineString(false)
                    .getEndPoint()
                    .getCoordinate();
        } else {
            return edgeIteratorState.fetchWayGeometry(FetchMode.ALL)
                    .toLineString(false)
                    .getStartPoint().getCoordinate();
        }
    }

    private static boolean isBidirectionalAndReverse(AdditionalSnap additionalSnap, boolean reverse) {
        return TrafficSignDirection.BOTH == additionalSnap.getTrafficSign().direction() && reverse;
    }

    private boolean edgeHasTrafficSigns(int linkId) {
        return snappedTrafficSignsByRoadSection.containsKey(linkId);
    }

    @Override
    public long calcEdgeMillis(EdgeIteratorState edgeIteratorState, boolean reverse) {
        return adaptedWeighting.calcEdgeMillis(edgeIteratorState, reverse);
    }

    @Override
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        return adaptedWeighting.calcTurnWeight(inEdge, viaNode, outEdge);
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return adaptedWeighting.calcTurnMillis(inEdge, viaNode, outEdge);
    }

    @Override
    public boolean hasTurnCosts() {
        return adaptedWeighting.hasTurnCosts();
    }

    @Override
    public String getName() {
        return adaptedWeighting.getName();
    }

    private int getLinkId(EdgeIteratorState edge) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
