package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;

@Builder
public record Restrictions(
        Set<TransportType> transportTypes,
        Maximum vehicleLengthInCm,
        Maximum vehicleHeightInCm,
        Maximum vehicleWidthInCm,
        Maximum vehicleWeightInKg,
        Maximum vehicleAxleLoadInKg) {

    public boolean hasActiveRestrictions(AccessibilityRequest accessibilityRequest) {
        return !getActiveRestrictions(accessibilityRequest).isEmpty();
    }

    public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {

        List<Predicate<AccessibilityRequest>> activeRestrictions = getActiveRestrictions(accessibilityRequest);
        if (activeRestrictions.isEmpty()) {
            return false;
        }

        return activeRestrictions.stream()
                .allMatch(restriction -> restriction.test(accessibilityRequest));
    }

    private List<Predicate<AccessibilityRequest>> getActiveRestrictions(AccessibilityRequest accessibilityRequest) {
        List<Predicate<AccessibilityRequest>> activeRestrictions = new ArrayList<>();

        if (Objects.nonNull(transportTypes) && Objects.nonNull(accessibilityRequest.transportTypes())) {
            activeRestrictions.add(containsTransportType());
        }

        if (Objects.nonNull(vehicleLengthInCm) && Objects.nonNull(accessibilityRequest.vehicleLengthInCm())) {
            activeRestrictions.add(isExceedingVehicleLength());
        }

        if (Objects.nonNull(vehicleWidthInCm) && Objects.nonNull(accessibilityRequest.vehicleWidthInCm())) {
            activeRestrictions.add(isExceedingVehicleWidth());
        }

        if (Objects.nonNull(vehicleHeightInCm) && Objects.nonNull(accessibilityRequest.vehicleHeightInCm())) {
            activeRestrictions.add(isExceedingVehicleHeight());
        }

        if (Objects.nonNull(vehicleWeightInKg) && Objects.nonNull(accessibilityRequest.vehicleWeightInKg())) {
            activeRestrictions.add(isExceedingVehicleWeight());
        }

        if (Objects.nonNull(vehicleAxleLoadInKg) && Objects.nonNull(accessibilityRequest.vehicleAxleLoadInKg())) {
            activeRestrictions.add(isExceedingVehicleAxleLoad());
        }

        return activeRestrictions;
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleLength() {
        return accessibilityRequest -> vehicleLengthInCm.isExceeding(accessibilityRequest.vehicleLengthInCm(), false);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleHeight() {
        return accessibilityRequest -> vehicleHeightInCm.isExceeding(accessibilityRequest.vehicleHeightInCm(), false);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleWidth() {
        return accessibilityRequest -> vehicleWidthInCm.isExceeding(accessibilityRequest.vehicleWidthInCm(), false);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleWeight() {
        return accessibilityRequest -> vehicleWeightInKg.isExceeding(accessibilityRequest.vehicleWeightInKg(), false);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleAxleLoad() {
        return accessibilityRequest -> vehicleAxleLoadInKg.isExceeding(accessibilityRequest.vehicleAxleLoadInKg(), false);
    }

    private Predicate<AccessibilityRequest> containsTransportType() {
        return accessibilityRequest -> transportTypes.stream().anyMatch(accessibilityRequest.transportTypes()::contains);
    }
}
