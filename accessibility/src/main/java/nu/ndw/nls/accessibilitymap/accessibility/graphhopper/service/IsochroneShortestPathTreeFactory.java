package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.TraversalMode;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsochroneByTimeDistanceAndWeight;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class IsochroneShortestPathTreeFactory {

    public static IsochroneByTimeDistanceAndWeight createIsochroneByTimeDistanceAndWeight(
            QueryGraph queryGraph,
            IsochroneArguments isochroneArguments
    ) {
        IsochroneByTimeDistanceAndWeight isochroneByTimeDistanceAndWeight = new IsochroneByTimeDistanceAndWeight(
                queryGraph,
                isochroneArguments.weighting(),
                false,
                TraversalMode.EDGE_BASED);

        isochroneByTimeDistanceAndWeight.setDistanceLimit(isochroneArguments.searchDistanceInMetres());
        return isochroneByTimeDistanceAndWeight;
    }
}
