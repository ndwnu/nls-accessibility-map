package nu.ndw.nls.accessibilitymap.backend;

import nu.ndw.nls.routingmapmatcher.config.MapMatcherConfiguration;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DatadogConfiguration.class, MapMatcherConfiguration.class})
@EnableCaching
public class AccessibilityMapApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessibilityMapApplication.class, args);
    }
}
