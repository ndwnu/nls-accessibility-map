package nu.ndw.nls.accessibilitymap.jobs.trafficsigns;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Value
@Validated
@ConfigurationProperties("nu.ndw.nls.accessibilitymap.jobs.trafficsigns")
public class TrafficSignProperties {


    @NotNull
    TrafficSignApiProperties api;

    @Value
    @Validated
    public static class TrafficSignApiProperties {
        @NotNull
        URI baseUrl;

        @NotBlank
        String currentStatePath;

        /**
         * Can be used for testing on a smaller subsets
         */
        String townCode;
    }

}
