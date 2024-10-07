package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto;

import com.graphhopper.storage.index.Snap;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.trafficsign.TrafficSign;

@Builder
@Getter
@ToString
public class AdditionalSnap {

    @NonNull
    private final Snap snap;

    private final TrafficSign trafficSign;
}
