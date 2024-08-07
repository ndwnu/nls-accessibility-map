package nu.ndw.nls.accessibilitymap.backend.graphhopper;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MUNICIPALITY_CODE;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsochroneByTimeDistanceAndWeight;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.ShortestPathTreeFactory;
import nu.ndw.nls.routingmapmatcher.isochrone.mappers.IsochroneMatchMapper;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.model.IsochroneUnit;
import org.locationtech.jts.geom.Point;

@RequiredArgsConstructor
public class IsochroneService {

    private static final int ROOT_PARENT = -1;
    private final EncodingManager encodingManager;
    private final BaseGraph baseGraph;
    private final IsochroneMatchMapper isochroneMatchMapper;
    private final ShortestPathTreeFactory shortestPathTreeFactory;
    private final LocationIndexTree locationIndexTree;

    /**
     * Creates Isochrone for an entire municipality based on start point. The start point has to be within the
     * municipality This can be used to create an accessibility map by first calling this method with a weighting that
     * has no restriction and consequently calling this method with a weighting that has restrictions based on vehicles
     * dimensions etc.
     *
     * @param weighting      The specific weighting ie Custom weighting to filter out inaccessible roads based on
     *                       vehicle dimensions.
     * @param startPoint     The start point of the isochrone search
     * @param municipalityId The id of the municipality to get the isochrone for
     * @return The list of isochrone matches
     * @see <a href="https://github.com/graphhopper/graphhopper/blob/master/docs/core/custom-models.md">Custom
     * models</a>
     */
    public List<IsochroneMatch> getIsochroneMatchesByMunicipalityId(Weighting weighting, Point startPoint,
            int municipalityId, double searchDistanceInMetres) {
        double latitude = startPoint.getY();
        double longitude = startPoint.getX();

        Snap startSegment = locationIndexTree.findClosest(latitude, longitude, EdgeFilter.ALL_EDGES);
        /*
            Lookup will create virtual edges based on the snapped point, thereby cutting the segment in 2 line strings.
            It also sets the closestNode of the matchedQueryResult to the virtual node id. In this way it creates a
            start point for isochrone calculation based on the snapped point coordinates.
        */
        QueryGraph queryGraph = QueryGraph.create(baseGraph, startSegment);
        IsochroneByTimeDistanceAndWeight accessibilityPathTree = shortestPathTreeFactory
                .createShortestPathTreeByTimeDistanceAndWeight(weighting, queryGraph, TraversalMode.EDGE_BASED,
                        searchDistanceInMetres, IsochroneUnit.METERS, false);
        List<IsoLabel> isoLabels = new ArrayList<>();
        accessibilityPathTree.search(startSegment.getClosestNode(), isoLabels::add);
        IntEncodedValue idEnc = encodingManager.getIntEncodedValue(MUNICIPALITY_CODE);
        return isoLabels.stream()
                .filter(isoLabel -> isoLabel.getEdge() != ROOT_PARENT)
                .filter(isoLabel -> getMunicipalityCode(isoLabel, queryGraph, idEnc) == municipalityId)
                .map(isoLabel -> isochroneMatchMapper.mapToIsochroneMatch(isoLabel, Double.POSITIVE_INFINITY,
                        queryGraph, startSegment.getClosestEdge()))
                .toList();
    }

    private int getMunicipalityCode(IsoLabel isoLabel, QueryGraph queryGraph, IntEncodedValue idEnc) {
        EdgeIteratorState currentEdge = queryGraph.getEdgeIteratorState(isoLabel.getEdge(), isoLabel.getNode());
        return currentEdge.get(idEnc);
    }
}
