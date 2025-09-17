package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.configuration;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("graphhopper")
@Value
@RequiredArgsConstructor
public class GraphHopperProperties {

    private static final String ACCESSIBILITY_METADATA_JSON = "accessibility_metadata.json";

    Path dir;

    String networkName;

    boolean publishEvents;

    public Path getLatestPath() {
        return getDir().resolve(getNetworkName());
    }

    public Path getMetadataPath() {
        return getLatestPath().resolve(ACCESSIBILITY_METADATA_JSON);
    }
}
