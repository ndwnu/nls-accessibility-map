package nu.ndw.nls.accessibilitymap.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AccessibilityMapApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessibilityMapApplication.class, args);
    }
}
