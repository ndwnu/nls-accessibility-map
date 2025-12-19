package nu.ndw.nls.accessibilitymap.jobs;

import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import nu.ndw.nls.springboot.job.JobApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@Import({JtsGeoJsonMappersConfiguration.class})
public class AccessibilityMapGeneratorJobApplication extends JobApplication {

    public static void main(String[] args) {
        run(AccessibilityMapGeneratorJobApplication.class, args);
    }
}
