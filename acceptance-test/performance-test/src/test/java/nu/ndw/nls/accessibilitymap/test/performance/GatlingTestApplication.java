package nu.ndw.nls.accessibilitymap.test.performance;

import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.springboot.gatling.test.GatlingConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({GatlingConfiguration.class, RoutingMapMatcherConfiguration.class})
public class GatlingTestApplication {

    public static void main(final String[] arguments) {
        SpringApplication.run(GatlingTestApplication.class, arguments);
    }
}
