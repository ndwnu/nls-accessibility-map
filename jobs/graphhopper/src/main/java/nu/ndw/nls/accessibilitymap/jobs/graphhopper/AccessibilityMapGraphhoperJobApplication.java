package nu.ndw.nls.accessibilitymap.jobs.graphhopper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccessibilityMapGraphhoperJobApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(AccessibilityMapGraphhoperJobApplication.class,args)));
    }
}
