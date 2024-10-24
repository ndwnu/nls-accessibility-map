package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "com.alliander.hedera.agent.test.driver.database")
public class DatabaseDriverConfiguration {

	private Duration asyncTimeout = Duration.ofSeconds(10);
}
