package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.QueryOverlayDataExtractor;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.QueryGraphWeighting;
import com.graphhopper.routing.weighting.Weighting;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsochroneByTimeDistanceAndWeight;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class IsochroneShortestPathTreeFactory {

    public static IsochroneByTimeDistanceAndWeight createIsochroneByTimeDistanceAndWeight(
            QueryGraph queryGraph,
            IsochroneArguments isochroneArguments) {

        Weighting queryGraphWeighting = new QueryGraphWeighting(
                queryGraph.getBaseGraph(),
                isochroneArguments.weighting(),
                QueryOverlayDataExtractor.getClosestEdges(queryGraph));

        IsochroneByTimeDistanceAndWeight isochroneByTimeDistanceAndWeight = new IsochroneByTimeDistanceAndWeight(
                queryGraph,
                queryGraphWeighting,
                false,
                TraversalMode.EDGE_BASED);

        isochroneByTimeDistanceAndWeight.setDistanceLimit(isochroneArguments.searchDistanceInMetres());
        return isochroneByTimeDistanceAndWeight;
    }
}
