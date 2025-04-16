package nu.ndw.nls.accessibilitymap.accessibility.nwb;

import nu.ndw.nls.db.nwb.EnableNwbDataAccessServices;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@EnableNwbDataAccessServices
@Import(RoutingMapMatcherConfiguration.class)
public class NwbConfiguration {

}
