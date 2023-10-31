package nu.ndw.nls.accessibilitymap.backend;

import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DatadogConfiguration.class)
public class AccessibilityMapApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessibilityMapApplication.class, args);
    }
}
