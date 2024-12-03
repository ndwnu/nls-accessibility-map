package nu.ndw.nls.accessibilitymap.shared.properties;


import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("graphhopper")
@Value
@RequiredArgsConstructor
public class GraphHopperProperties {

    private static final String ACCESSIBILITY_META_DATA_JSON = "accessibility_meta_data.json";
    Path dir;
    String networkName;
    boolean withTrafficSigns;
    boolean publishEvents;

    public Path getLatestPath() {
        return getDir().resolve(getNetworkName());
    }

    public Path getMetaDataPath() {
        return getLatestPath().resolve(ACCESSIBILITY_META_DATA_JSON);
    }
}
