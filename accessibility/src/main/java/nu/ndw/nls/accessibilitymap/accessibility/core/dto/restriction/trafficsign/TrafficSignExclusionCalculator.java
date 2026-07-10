package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.Objects;
import java.util.function.BiPredicate;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TrafficSignExclusionCalculator {

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasExclusionsForZoneCodeTypes =
            (trafficSign, accessibilityRequest) -> {
                if (Objects.isNull(trafficSign.zoneCodeType())) {
                    return false;
                }

                return accessibilityRequest.excludeTrafficSignZoneCodeTypes().contains(trafficSign.zoneCodeType());
            };

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasExclusionsForSupplementarySignTypes =
            (trafficSign, accessibilityRequest) -> trafficSign.supplementaryTrafficSigns().stream()
                    .map(SupplementaryTrafficSign::type)
                    .filter(Objects::nonNull)
                    .anyMatch(textSignType -> accessibilityRequest.excludeTrafficSignSupplementarySignTypes().contains(textSignType));

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasExclusionsForEmissionZoneId =
            (trafficSign, accessibilityRequest) -> {
                if (Objects.isNull(accessibilityRequest.excludeRestrictionsWithEmissionZoneIds())
                    || Objects.isNull(trafficSign.transportRestrictions().emissionZone())) {
                    return false;
                }

                return accessibilityRequest.excludeRestrictionsWithEmissionZoneIds()
                        .contains(trafficSign.transportRestrictions().emissionZone().id());
            };

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasExclusionsForEmissionZoneType =
            (trafficSign, accessibilityRequest) -> {
                if (Objects.isNull(accessibilityRequest.excludeRestrictionsWithEmissionZoneTypes())
                    || Objects.isNull(trafficSign.transportRestrictions().emissionZone())) {
                    return false;
                }

                return accessibilityRequest.excludeRestrictionsWithEmissionZoneTypes()
                        .contains(trafficSign.transportRestrictions().emissionZone().type());
            };

    private static final BiPredicate<TrafficSign, AccessibilityRequest> isOutsideOfBoundingBox =
            (trafficSign, accessibilityRequest) ->
                    !accessibilityRequest.searchArea().contains(trafficSign.latitude(), trafficSign.longitude());

    public static boolean isNotExcluded(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        return !hasExclusionsForZoneCodeTypes
                .or(hasExclusionsForSupplementarySignTypes)
                .or(hasExclusionsForEmissionZoneId)
                .or(hasExclusionsForEmissionZoneType)
                .or(isOutsideOfBoundingBox)
                .test(trafficSign, accessibilityRequest);
    }
}
