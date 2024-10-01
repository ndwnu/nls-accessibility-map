package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.dto;

import com.graphhopper.storage.index.Snap;
import java.util.Map;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSign;

@Builder
public record AdditionalSnap(
        Snap snap,
        TrafficSign trafficSign,
        Map<String, Boolean> overrulingProperties
) {
}
