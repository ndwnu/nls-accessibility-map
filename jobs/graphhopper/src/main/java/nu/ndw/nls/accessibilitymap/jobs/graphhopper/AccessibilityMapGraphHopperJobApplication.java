package nu.ndw.nls.accessibilitymap.jobs.graphhopper;

import nu.ndw.nls.springboot.messaging.MessagingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({MessagingConfig.class})
@SpringBootApplication
public class AccessibilityMapGraphHopperJobApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(AccessibilityMapGraphHopperJobApplication.class, args)));
    }
}
