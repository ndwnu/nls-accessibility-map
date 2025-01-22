package nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto;

import com.graphhopper.storage.index.Snap;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Getter
@ToString
@Validated
@EqualsAndHashCode
public class TrafficSignSnap {

    @NotNull
    private final Snap snap;

    private final TrafficSign trafficSign;
}
