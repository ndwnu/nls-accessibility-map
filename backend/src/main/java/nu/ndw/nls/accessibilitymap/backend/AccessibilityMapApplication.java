package nu.ndw.nls.accessibilitymap.backend;

import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = RabbitAutoConfiguration.class)
@Import({JtsGeoJsonMappersConfiguration.class})
@ComponentScan(
        basePackages = {"nu.ndw.nls.accessibilitymap.backend", "nu.ndw.nls.accessibilitymap.generated"},
        excludeFilters = {
                @Filter(
                        type = FilterType.CUSTOM,
                        classes = {TypeExcludeFilter.class}
                ),
                @Filter(
                        type = FilterType.CUSTOM,
                        classes = {AutoConfigurationExcludeFilter.class}
                ),
        })
public class AccessibilityMapApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessibilityMapApplication.class, args);
    }
}
