package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.geometry.GeometryConfiguration;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import({
        GeometryConfiguration.class, RoutingMapMatcherConfiguration.class,
        GraphHopperNetworkSettingsBuilder.class, GraphHopperNetworkService.class, NetworkMetaDataService.class})
public class GraphhopperWriteConfiguration {

}
