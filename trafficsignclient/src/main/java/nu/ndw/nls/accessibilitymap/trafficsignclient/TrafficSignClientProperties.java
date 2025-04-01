package nu.ndw.nls.accessibilitymap.trafficsignclient;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Value
@Validated
@ConfigurationProperties("nu.ndw.nls.accessibilitymap.trafficsignclient")
public class TrafficSignClientProperties {


    @NotNull
    TrafficSignApiProperties api = new TrafficSignApiProperties();

    @Value
    @Validated
    public static class TrafficSignApiProperties {

        /**
         * Can be used for testing on a smaller subsets
         */
        Set<String> townCodes = new HashSet<>();
    }

}
