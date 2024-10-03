package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto;

import com.graphhopper.storage.index.Snap;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;

@Builder
@Getter
@ToString
public class AdditionalSnap {

    @NonNull
    private final Snap snap;

    private final TrafficSign trafficSign;
}
