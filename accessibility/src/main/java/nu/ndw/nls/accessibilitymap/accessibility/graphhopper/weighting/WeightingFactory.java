package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.PMap;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import org.springframework.stereotype.Component;

@Component
public class WeightingFactory {

    public Weighting createWeighting(
            NetworkData networkData,
            QueryGraph queryGraph,
            Set<Integer> blockedEdges
    ) {
        var baseWeighting = networkData.getNetworkGraphHopper().createWeighting(NetworkConstants.CAR_PROFILE, new PMap());

        var restrictionWeightingDecorator = new RestrictionWeighting(baseWeighting, blockedEdges);

        var roadDataWeightingDecorator = new RoadDataWeighting(
                restrictionWeightingDecorator,
                networkData.getNwbData(),
                networkData.getNetworkGraphHopper().getEncodingManager());

        var roadChangesWeightingDecorator = new RoadChangesWeighting(
                roadDataWeightingDecorator,
                networkData.getNwbDataUpdates(),
                networkData.getNetworkGraphHopper().getEncodingManager());

        return queryGraph.wrapWeighting(roadChangesWeightingDecorator);
    }
}
