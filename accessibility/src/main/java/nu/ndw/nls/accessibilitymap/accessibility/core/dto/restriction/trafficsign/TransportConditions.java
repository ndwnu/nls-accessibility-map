package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.apache.commons.collections4.CollectionUtils;

@Builder
public record TransportConditions(
        Set<TransportType> transportTypes, // vehicleType
        Set<Category> category,
        String timeValidity,
        int emissionClass,
        String fuelType,
        Maximum vehicleLengthInCm,
        Maximum vehicleHeightInCm,
        Maximum vehicleWidthInCm,
        Maximum vehicleWeightInKg,
        Maximum vehicleAxleLoadInKg,
        EmissionZone emissionZone
        ) {

    public boolean hasEvaluableConditions(AccessibilityRequest accessibilityRequest) {

        return !getActiveConditions(accessibilityRequest).isEmpty();
    }

    public boolean conditionsApply(AccessibilityRequest accessibilityRequest) {

        List<Predicate<AccessibilityRequest>> activeConditions = getActiveConditions(accessibilityRequest);
        if (activeConditions.isEmpty()) {
            return false;
        }

        return activeConditions.stream()
                .allMatch(restriction -> restriction.test(accessibilityRequest));
    }

    private List<Predicate<AccessibilityRequest>> getActiveConditions(AccessibilityRequest accessibilityRequest) {

        List<Predicate<AccessibilityRequest>> activeRestrictions = new ArrayList<>();

        if (Objects.nonNull(emissionZone)
                && emissionZone.isActive(accessibilityRequest.timestamp())
                && Objects.nonNull(accessibilityRequest.fuelTypes())
                && Objects.nonNull(accessibilityRequest.emissionClasses())) {
            activeRestrictions.add(buildEmissionRestriction());
        }

        if (CollectionUtils.isNotEmpty(transportTypes) && Objects.nonNull(accessibilityRequest.transportTypes())) {
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

    private Predicate<AccessibilityRequest> buildEmissionRestriction() {
        return accessibilityRequest -> {

            if (emissionZone.isRelevant(
                    accessibilityRequest.vehicleWeightInKg(),
                    accessibilityRequest.fuelTypes(),
                    accessibilityRequest.transportTypes())) {

                return !emissionZone.isExempt(
                        accessibilityRequest.timestamp(),
                        accessibilityRequest.vehicleWeightInKg(),
                        accessibilityRequest.emissionClasses(),
                        accessibilityRequest.transportTypes());
            }
            return false;
        };
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
