package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.PMap;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.RoadChanges;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
public class WeightingFactory {

    public Weighting createWeighting(
            NetworkData networkData,
            QueryGraph queryGraph,
            Set<Integer> blockedEdges,
            RoadChanges roadChanges,
            boolean applyRestrictions
    ) {
        NetworkGraphHopper networkGraphHopper = networkData.getNetworkGraphHopper();
        var baseWeighting = networkGraphHopper
                .createWeighting(NetworkConstants.CAR_PROFILE, new PMap());
        var restrictionWeightingDecorator = new RestrictionWeightingDecorator(baseWeighting,
                applyRestrictions ? blockedEdges : java.util.Set.of());
        var roadDataWeightingDecorator = new RoadDataWeightingDecorator(restrictionWeightingDecorator,
                networkData.getNwbData());
        var roadChangesWeightingDecorator = new RoadChangesWeightingDecorator(roadDataWeightingDecorator,
                roadChanges);
        return queryGraph.wrapWeighting(roadChangesWeightingDecorator);
    }
}
