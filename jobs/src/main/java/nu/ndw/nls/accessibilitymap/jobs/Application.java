package nu.ndw.nls.accessibilitymap.jobs;

import nu.ndw.nls.routingmapmatcher.config.MapMatcherConfiguration;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({MapMatcherConfiguration.class, MessagingConfig.class})
public class Application {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(Application.class, args)));
    }
}
