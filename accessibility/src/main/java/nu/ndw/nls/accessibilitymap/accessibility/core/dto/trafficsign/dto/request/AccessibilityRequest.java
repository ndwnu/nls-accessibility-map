package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.request;

import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value.TransportType;

@Builder
@With
public record AccessibilityRequest(
        Double vehicleLength,
        Double vehicleHeight,
        Double vehicleWidth,
        Double vehicleWeight,
        Double vehicleAxleLoad,
        TransportType transportType
) {

}
