package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.dto;

import com.graphhopper.storage.index.Snap;
import java.util.Map;
import lombok.Builder;

@Builder
public record AdditionalSnap(
        Snap snap,
        Map<String, Boolean> overrulingProperties
) {
}
