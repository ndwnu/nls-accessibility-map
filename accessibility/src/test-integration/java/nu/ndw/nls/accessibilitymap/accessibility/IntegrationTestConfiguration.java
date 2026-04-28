package nu.ndw.nls.accessibilitymap.accessibility;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
        basePackages = "nu.ndw.nls.accessibilitymap",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.CUSTOM,
                classes = TypeExcludeFilter.class
        )
)
public class IntegrationTestConfiguration {

}
