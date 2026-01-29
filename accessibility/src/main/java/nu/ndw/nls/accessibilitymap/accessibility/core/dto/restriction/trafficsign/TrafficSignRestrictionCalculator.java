package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.Objects;
import java.util.function.BiPredicate;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TrafficSignRestrictionCalculator {

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasTransportRestrictions =
            (trafficSign, accessibilityRequest) -> {
                if (!trafficSign.transportRestrictions().hasActiveRestrictions(accessibilityRequest)) {
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

    private static final BiPredicate<TrafficSign, AccessibilityRequest> hasTrafficSignTextTypes =
            (trafficSign, accessibilityRequest) -> {
                if (Objects.isNull(accessibilityRequest.trafficSignTextSignTypes())) {
                    return false;
                }

                return trafficSign.textSigns().stream()
                        .map(TextSign::type)
                        .filter(Objects::nonNull)
                        .anyMatch(accessibilityRequest.trafficSignTextSignTypes()::contains);
            };

    public static boolean isRestrictive(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        return hasTransportRestrictions
                .or(hasTrafficSignType)
                .or(hasTrafficSignTextTypes)
                .test(trafficSign, accessibilityRequest);
    }

}
