package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.Objects;
import java.util.function.BiPredicate;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TrafficSignRestrictionCalculator {

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasTransportRestrictions =
            (trafficSign, accessibilityRequest) -> {
                if (!trafficSign.transportRestrictions().isRestrictive(accessibilityRequest)) {
                    return false;
                }

                return trafficSign.transportRestrictions().isRestrictive(accessibilityRequest);
            };

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasTrafficSignType =
            (trafficSign, accessibilityRequest) -> {
                if (Objects.isNull(accessibilityRequest.trafficSignTypes())) {
                    return false;
                }

                return accessibilityRequest.trafficSignTypes().contains(trafficSign.trafficSignType());
            };

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasSupplementarySignTypes =
            (trafficSign, accessibilityRequest) -> {
                if (Objects.isNull(accessibilityRequest.trafficSignSupplementarySignTypes())) {
                    return false;
                }

                return trafficSign.supplementaryTrafficSigns()
                        .stream()
                        .map(SupplementaryTrafficSign::type)
                        .anyMatch(o -> accessibilityRequest.trafficSignSupplementarySignTypes().contains(o));
            };

    public static boolean isRestrictive(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        return hasTransportRestrictions
                .or(hasTrafficSignType)
                .or(hasSupplementarySignTypes)
                .test(trafficSign, accessibilityRequest);
    }
}
