package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto;

import com.graphhopper.storage.index.Snap;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSign;

@Builder
public class AdditionalSnap {

    @NonNull
    private final Snap snap;

    private final TrafficSign trafficSign;

    @Default
    private final Map<String, Boolean> overrulingProperties = new HashMap<>();

}
