package nu.ndw.nls.accessibilitymap.test.acceptance;

import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.springboot.test.component.driver.docker.EnableDockerDriver;
import nu.ndw.nls.springboot.test.component.driver.keycloak.EnableKeycloakDriver;
import nu.ndw.nls.springboot.test.component.driver.maven.EnableMavenDriver;
import nu.ndw.nls.springboot.test.component.driver.wiremock.EnableWireMockDriver;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("nu.ndw.nls.accessibilitymap.test.acceptance")
@EnableJpaRepositories("nu.ndw.nls.accessibilitymap.test.acceptance.driver.database")
@EntityScan(basePackages = "nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity")
@EnableKeycloakDriver
@EnableDockerDriver
@EnableWireMockDriver
@EnableMavenDriver
@Import(RoutingMapMatcherConfiguration.class)
public class AcceptanceTestSharedAutoconfiguration {

}
