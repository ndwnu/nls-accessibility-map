package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.weight;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.PMap;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeightingFactory {

    public final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    public Weighting createWeighting(QueryGraph queryGraph, NetworkData networkData, SpeedLimits speedLimits) {

        Weighting baseWeighting = networkData.getNetworkGraphHopper().createWeighting(NetworkConstants.CAR_PROFILE, new PMap());

        VariableSpeedLimitWeighting variableSpeedLimitWeighting = new VariableSpeedLimitWeighting(
                baseWeighting,
                speedLimits,
                networkData.getNetworkGraphHopper().getEncodingManager(),
                edgeIteratorStateReverseExtractor);

        return queryGraph.wrapWeighting(variableSpeedLimitWeighting);
    }
}
