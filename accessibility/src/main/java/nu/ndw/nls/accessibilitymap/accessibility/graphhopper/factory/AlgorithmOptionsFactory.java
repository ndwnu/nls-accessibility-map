package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.util.PMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.configuration.GraphHopperRoutingProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlgorithmOptionsFactory {

    private final GraphHopperRoutingProperties routingProperties;

    public AlgorithmOptions createAlgorithmOptions() {
        AlgorithmOptions algorithmOptions = new AlgorithmOptions();
        algorithmOptions.setAlgorithm(routingProperties.getAlgorithm());
        algorithmOptions.setTraversalMode(routingProperties.getTraversalMode());
        if (routingProperties.getHints() != null) {
            algorithmOptions.setHints(createPMap(routingProperties.getHints()));
        }
        return algorithmOptions;
    }

    private PMap createPMap(Map<String, Object> hints) {
        return new PMap(hints);
    }

}
