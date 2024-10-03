package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto;

import com.graphhopper.storage.index.Snap;
import lombok.Builder;
import lombok.NonNull;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;

@Builder
public class AdditionalSnap {

    @NonNull
    private final Snap snap;

    private final TrafficSign trafficSign;
}
