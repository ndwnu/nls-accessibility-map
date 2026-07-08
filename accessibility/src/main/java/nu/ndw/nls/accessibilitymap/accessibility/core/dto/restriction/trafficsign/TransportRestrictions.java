package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.List;
import java.util.Objects;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;

@Builder
public record TransportRestrictions(TransportConditions restrictions, List<TransportConditions> exemptions, EmissionZone emissionZone) {

    public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {
        if (emissionZoneRestrictionsApply(accessibilityRequest)) {
            return true;
        }

        if (restrictions == null ||
            !restrictions.conditionsApply(accessibilityRequest)) {
            return false;
        }

        return exemptions.stream().noneMatch(exemption -> exemption.conditionsApply(accessibilityRequest));
    }

    private boolean emissionZoneRestrictionsApply(AccessibilityRequest accessibilityRequest) {
        if (Objects.nonNull(emissionZone)
            && emissionZone.isActive(accessibilityRequest.timestamp())
            && Objects.nonNull(accessibilityRequest.fuelTypes())
            && Objects.nonNull(accessibilityRequest.emissionClasses())) {

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

        }

        return false;
    }
}
