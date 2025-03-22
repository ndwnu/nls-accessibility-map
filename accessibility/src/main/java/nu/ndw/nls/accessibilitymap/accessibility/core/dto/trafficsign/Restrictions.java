package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value.Range;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value.TransportType;

@Builder
public record Restrictions(
        Boolean isBlocked,
        List<TransportType> transportTypes,
        Range vehicleLength,
        Range vehicleHeight,
        Range vehicleWidth,
        Range vehicleWeight,
        Range vehicleAxleLoad
) {

    public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {

        return isRestricted()
                .or(isNotWithinVehicleLength())
                .or(isNotWithinVehicleHeight())
                .or(isNotWithinVehicleWidth())
                .or(isNotWithinVehicleWeight())
                .or(isNotWithinVehicleAxleLoad())
                .or(containsTransportType())
                .test(accessibilityRequest);
    }

    private Predicate<AccessibilityRequest> isRestricted() {
        return accessibilityRequest -> !Objects.isNull(isBlocked) && isBlocked;
    }

    private Predicate<AccessibilityRequest> isNotWithinVehicleLength() {
        return accessibilityRequest -> !Objects.isNull(vehicleLength)
                && vehicleLength.isNotWithin(accessibilityRequest.vehicleLength(), true);
    }

    private Predicate<AccessibilityRequest> isNotWithinVehicleHeight() {
        return accessibilityRequest -> !Objects.isNull(vehicleHeight)
                && vehicleHeight.isNotWithin(accessibilityRequest.vehicleHeight(), true);
    }

    private Predicate<AccessibilityRequest> isNotWithinVehicleWidth() {
        return accessibilityRequest -> !Objects.isNull(vehicleWidth)
                && vehicleWidth.isNotWithin(accessibilityRequest.vehicleWidth(), true);
    }

    private Predicate<AccessibilityRequest> isNotWithinVehicleWeight() {
        return accessibilityRequest -> !Objects.isNull(vehicleWeight)
                && vehicleWeight.isNotWithin(accessibilityRequest.vehicleWeight(), true);
    }

    private Predicate<AccessibilityRequest> isNotWithinVehicleAxleLoad() {
        return accessibilityRequest -> !Objects.isNull(vehicleAxleLoad)
                && vehicleAxleLoad.isNotWithin(accessibilityRequest.vehicleAxleLoad(), true);
    }

    private Predicate<AccessibilityRequest> containsTransportType() {
        return accessibilityRequest ->
                !Objects.isNull(transportTypes)
                        && transportTypes.contains(accessibilityRequest.transportType());
    }
}
