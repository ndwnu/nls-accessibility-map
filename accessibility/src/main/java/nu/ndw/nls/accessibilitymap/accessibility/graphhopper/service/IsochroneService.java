package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink.MUNICIPALITY_CODE;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsochroneByTimeDistanceAndWeight;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.ShortestPathTreeFactory;
import nu.ndw.nls.routingmapmatcher.isochrone.mappers.IsochroneMatchMapper;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.model.IsochroneUnit;

@RequiredArgsConstructor
public class IsochroneService {

    private static final int ROOT_PARENT = -1;

    private final EncodingManager encodingManager;

    private final IsochroneMatchMapper isochroneMatchMapper;

    private final ShortestPathTreeFactory shortestPathTreeFactory;

    /**
     * Creates Isochrone for an entire municipality based on start point. The start point has to be within the municipality This can be used
     * to create an accessibility map by first calling this method with a weighting that has no restriction and consequently calling this
     * method with a weighting that has restrictions based on vehicles dimensions etc.
     *
     * @param isochroneArguments The isochrone arguments
     * @return The list of isochrone matches
     * @see <a href="https://github.com/graphhopper/graphhopper/blob/master/docs/core/custom-models.md">Custom
     * models</a>
     */
    public List<IsochroneMatch> getIsochroneMatchesByMunicipalityId(
            IsochroneArguments isochroneArguments,
            QueryGraph queryGraph,
            Snap startSegment) {
        int matchedLinkId = getLinkId(startSegment.getClosestEdge());
        IsochroneByTimeDistanceAndWeight accessibilityPathTree = shortestPathTreeFactory
                .createShortestPathTreeByTimeDistanceAndWeight(isochroneArguments.weighting(), queryGraph,
                        TraversalMode.EDGE_BASED, isochroneArguments.searchDistanceInMetres(), IsochroneUnit.METERS,
                        false, false, matchedLinkId);
        List<IsoLabel> isoLabels = new ArrayList<>();
        accessibilityPathTree.search(startSegment.getClosestNode(), isoLabels::add);

        return isoLabels.stream()
                .filter(isoLabel -> isoLabel.getEdge() != ROOT_PARENT)
                .filter(isoLabel -> filterMunicipality(queryGraph, isoLabel, isochroneArguments))
                .map(isoLabel -> isochroneMatchMapper.mapToIsochroneMatch(isoLabel, Double.POSITIVE_INFINITY,
                        queryGraph, startSegment.getClosestEdge(), false))
                .toList();
    }

    private boolean filterMunicipality(
            QueryGraph queryGraph,
            IsoLabel isoLabel,
            IsochroneArguments isochroneArguments) {

        if (isochroneArguments.getMunicipalityId().isEmpty()) {
            return true;
        }

        IntEncodedValue idEnc = encodingManager.getIntEncodedValue(MUNICIPALITY_CODE);
        return getMunicipalityCode(isoLabel, queryGraph, idEnc) == isochroneArguments.municipalityId();
    }

    private int getMunicipalityCode(IsoLabel isoLabel, QueryGraph queryGraph, IntEncodedValue idEnc) {
        EdgeIteratorState currentEdge = queryGraph.getEdgeIteratorState(isoLabel.getEdge(), isoLabel.getNode());
        return currentEdge.get(idEnc);
    }

    private int getLinkId(EdgeIteratorState edge) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
