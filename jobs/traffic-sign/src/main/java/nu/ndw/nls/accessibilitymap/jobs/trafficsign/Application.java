package nu.ndw.nls.accessibilitymap.jobs.trafficsign;

import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@Import({JtsGeoJsonMappersConfiguration.class})
@EnableFeignClients
public class Application {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(Application.class, args)));
    }
}
