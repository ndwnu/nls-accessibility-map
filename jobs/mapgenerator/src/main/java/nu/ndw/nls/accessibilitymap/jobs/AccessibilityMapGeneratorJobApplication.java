package nu.ndw.nls.accessibilitymap.jobs;

import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@Import({JtsGeoJsonMappersConfiguration.class})
public class AccessibilityMapGeneratorJobApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(AccessibilityMapGeneratorJobApplication.class, args)));
    }
}
