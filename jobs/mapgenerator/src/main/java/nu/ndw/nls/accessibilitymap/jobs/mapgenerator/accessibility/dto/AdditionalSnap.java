package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto;

import com.graphhopper.storage.index.Snap;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.With;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Getter
@ToString
@Validated
public class AdditionalSnap {

    @NotNull
    private final Snap snap;

    private final TrafficSign trafficSign;
}
