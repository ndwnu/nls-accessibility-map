package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.TransportTypeReason;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsTrafficSignRestrictionMapper implements RestrictionMapper {

    public List<AccessibilityReason<?>> mapRestrictions(Restrictions restrictions) {

        return restrictions.stream()
                .filter(TrafficSign.class::isInstance)
                .map(TrafficSign.class::cast)
                .flatMap(trafficSignRestriction -> Stream.of(
                                mapVehicleHeight(trafficSignRestriction),
                                mapVehicleWidth(trafficSignRestriction),
                                mapVehicleLength(trafficSignRestriction),
                                mapVehicleAxleLoad(trafficSignRestriction),
                                mapVehicleWeight(trafficSignRestriction),
                                mapTransportTypes(trafficSignRestriction),
                                mapEmissionZoneRestrictions(trafficSignRestriction))
                        .flatMap(List::stream))
                .toList();
    }

    private static List<AccessibilityReason<?>> mapVehicleHeight(TrafficSign trafficSign) {

        if (Objects.isNull(trafficSign.transportRestrictions().vehicleHeightInCm())) {
            return List.of();
        }

        return List.of(MaximumReason.builder()
                .value(trafficSign.transportRestrictions().vehicleHeightInCm())
                .restrictions(Set.of(trafficSign))
                .reasonType(ReasonType.VEHICLE_HEIGHT)
                .build());
    }

    private static List<AccessibilityReason<?>> mapVehicleWidth(TrafficSign trafficSign) {

        if (Objects.isNull(trafficSign.transportRestrictions().vehicleWidthInCm())) {
            return List.of();
        }

        return List.of(MaximumReason.builder()
                .value(trafficSign.transportRestrictions().vehicleWidthInCm())
                .restrictions(Set.of(trafficSign))
                .reasonType(ReasonType.VEHICLE_WIDTH)
                .build());
    }

    private static List<AccessibilityReason<?>> mapVehicleLength(TrafficSign trafficSign) {

        if (Objects.isNull(trafficSign.transportRestrictions().vehicleLengthInCm())) {
            return List.of();
        }

        return List.of(MaximumReason.builder()
                .value(trafficSign.transportRestrictions().vehicleLengthInCm())
                .restrictions(Set.of(trafficSign))
                .reasonType(ReasonType.VEHICLE_LENGTH)
                .build());
    }

    private static List<AccessibilityReason<?>> mapVehicleAxleLoad(TrafficSign trafficSign) {

        if (Objects.isNull(trafficSign.transportRestrictions().vehicleAxleLoadInKg())) {
            return List.of();
        }

        return List.of(MaximumReason.builder()
                .value(trafficSign.transportRestrictions().vehicleAxleLoadInKg())
                .restrictions(Set.of(trafficSign))
                .reasonType(ReasonType.VEHICLE_AXLE_LOAD)
                .build());
    }

    private static List<AccessibilityReason<?>> mapVehicleWeight(TrafficSign trafficSign) {

        if (Objects.isNull(trafficSign.transportRestrictions().vehicleWeightInKg())) {
            return List.of();
        }

        return List.of(MaximumReason.builder()
                .value(trafficSign.transportRestrictions().vehicleWeightInKg())
                .restrictions(Set.of(trafficSign))
                .reasonType(ReasonType.VEHICLE_WEIGHT)
                .build());
    }

    private static List<AccessibilityReason<?>> mapTransportTypes(TrafficSign trafficSign) {

        if (Objects.isNull(trafficSign.transportRestrictions().transportTypes())
            || trafficSign.transportRestrictions().transportTypes().isEmpty()) {
            return List.of();
        }

        return List.of(TransportTypeReason.builder()
                .value(trafficSign.transportRestrictions().transportTypes())
                .restrictions(Set.of(trafficSign))
                .build());
    }

    @SuppressWarnings("java:S1854")
    private static List<AccessibilityReason<?>> mapEmissionZoneRestrictions(TrafficSign trafficSign) {

        List<AccessibilityReason<?>> accessibilityReasons = new ArrayList<>();

        if (Objects.isNull(trafficSign.transportRestrictions().emissionZone())) {
            return accessibilityReasons;
        }

        EmissionZoneRestriction restriction = trafficSign.transportRestrictions().emissionZone().restriction();
        if (Objects.nonNull(restriction.vehicleWeightInKg())) {
            accessibilityReasons.add(MaximumReason.builder()
                    .value(restriction.vehicleWeightInKg())
                    .restrictions(Set.of(trafficSign))
                    .reasonType(ReasonType.VEHICLE_WEIGHT)
                    .build());
        }

        if (!restriction.fuelTypes().isEmpty()) {
            accessibilityReasons.add(FuelTypeReason.builder()
                    .value(restriction.fuelTypes())
                    .restrictions(Set.of(trafficSign))
                    .build());
        }

        if (!restriction.transportTypes().isEmpty()) {
            accessibilityReasons.add(TransportTypeReason.builder()
                    .value(restriction.transportTypes())
                    .restrictions(Set.of(trafficSign))
                    .build());
        }

        return accessibilityReasons;
    }
}
