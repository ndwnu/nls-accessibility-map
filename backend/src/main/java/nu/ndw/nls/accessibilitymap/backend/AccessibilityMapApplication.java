package nu.ndw.nls.accessibilitymap.backend;

import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = RabbitAutoConfiguration.class)
@Import({JtsGeoJsonMappersConfiguration.class})
public class AccessibilityMapApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessibilityMapApplication.class, args);
    }
}
