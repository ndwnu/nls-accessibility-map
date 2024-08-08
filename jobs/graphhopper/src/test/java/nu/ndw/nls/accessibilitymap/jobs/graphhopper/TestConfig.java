package nu.ndw.nls.accessibilitymap.jobs.graphhopper;

import nu.ndw.nls.springboot.messaging.MessagingConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import(MessagingConfig.class)
public class TestConfig {

}
