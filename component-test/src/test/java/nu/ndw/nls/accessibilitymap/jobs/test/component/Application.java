package nu.ndw.nls.accessibilitymap.jobs.test.component;

import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import nu.ndw.nls.springboot.test.component.driver.keycloak.EnableKeycloakDriver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@Import({RoutingMapMatcherConfiguration.class, MessagingConfig.class})
@EnableKeycloakDriver
public class Application {

    public static void main(final String[] arguments) {

        SpringApplication.run(Application.class, arguments);
    }
}
