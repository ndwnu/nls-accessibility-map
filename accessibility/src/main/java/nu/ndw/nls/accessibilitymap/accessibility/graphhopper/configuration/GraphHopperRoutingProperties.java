package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.configuration;

import com.graphhopper.routing.util.TraversalMode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("graphhopper.routing")
@Value
@RequiredArgsConstructor
public class GraphHopperRoutingProperties {
    // @see com.graphhopper.util.Parameters.Algorithms
    private final String algorithm;
    private TraversalMode traversalMode;
    // @see com.graphhopper.util.Parameters.CH
    private final boolean chDisable;
    //@see com.graphhopper.util.Parameters.Routing or com.graphhopper.util.Parameters.AltRoute
    private Map<String, Object> hints;


}
