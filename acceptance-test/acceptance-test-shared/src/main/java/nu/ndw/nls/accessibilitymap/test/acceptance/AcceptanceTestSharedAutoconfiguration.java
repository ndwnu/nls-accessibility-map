package nu.ndw.nls.accessibilitymap.test.acceptance;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("nu.ndw.nls.accessibilitymap.test.acceptance")
@EnableJpaRepositories("nu.ndw.nls.accessibilitymap.test.acceptance.driver.database")
@EntityScan(basePackages = "nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity")
public class AcceptanceTestSharedAutoconfiguration {

}
