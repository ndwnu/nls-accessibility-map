package nu.ndw.nls.accessibilitymap.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccessibilityMapGeneratorJobApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(AccessibilityMapGeneratorJobApplication.class, args)));
    }
}
