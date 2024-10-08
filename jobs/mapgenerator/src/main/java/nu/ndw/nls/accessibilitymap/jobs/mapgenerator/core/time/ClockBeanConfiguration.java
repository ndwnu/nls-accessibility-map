package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockBeanConfiguration {

	@Bean
	public Clock clock() {

		return Clock.systemUTC();
	}
}
