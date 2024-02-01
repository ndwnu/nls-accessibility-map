package nu.ndw.nls.accessibilitymap.shared.properties;


import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("graphhopper")
@Value
@RequiredArgsConstructor
public class GraphHopperProperties {

    Path dir;

}
