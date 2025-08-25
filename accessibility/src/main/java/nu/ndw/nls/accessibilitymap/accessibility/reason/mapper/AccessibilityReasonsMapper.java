package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import static java.util.Map.entry;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C10;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C11;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C12;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C17;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C18;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C19;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C20;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C21;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C22;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C22A;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C22C;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C6;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7A;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7B;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C8;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C9;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsMapper {

    @SuppressWarnings("java:S3740")
    private static final Map<TrafficSignType, Function<TrafficSign, List<AccessibilityRestriction>>> RESTRICTION_MAPPERS = Map.ofEntries(
            entry(C1, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C6, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C7, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C7A, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C7B, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C8, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C9, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C10, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C11, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C12, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C17, AccessibilityReasonsMapper::mapVehicleLength),
            entry(C18, AccessibilityReasonsMapper::mapVehicleWidth),
            entry(C19, AccessibilityReasonsMapper::mapVehicleHeight),
            entry(C20, AccessibilityReasonsMapper::mapVehicleAxleLoad),
            entry(C21, AccessibilityReasonsMapper::mapVehicleWeight),
            entry(C22, AccessibilityReasonsMapper::mapTransportTypes),
            entry(C22A, AccessibilityReasonsMapper::mapEmissionZoneRestrictions),
            entry(C22C, AccessibilityReasonsMapper::mapEmissionZoneRestrictions)
    );

    public AccessibilityReasons mapToAoAccessibilityReasons(List<TrafficSign> trafficSigns) {

        return new AccessibilityReasons(trafficSigns.stream()
                .map(this::mapTrafficSignToAccessibilityReason)
                .toList());
    }

    @SuppressWarnings("java:S3740")
    private AccessibilityReason mapTrafficSignToAccessibilityReason(TrafficSign trafficSign) {

        if (!RESTRICTION_MAPPERS.containsKey(trafficSign.trafficSignType())) {
            throw new IllegalArgumentException("Traffic sign type " + trafficSign.trafficSignType() + " is not supported");
        }
        List<AccessibilityRestriction> restrictions = RESTRICTION_MAPPERS.get(trafficSign.trafficSignType()).apply(trafficSign);
        AccessibilityReason reason = AccessibilityReason.builder()
                .trafficSignExternalId(trafficSign.externalId())
                .direction(trafficSign.direction())
                .trafficSignType(trafficSign.trafficSignType())
                .roadSectionId(trafficSign.roadSectionId())
                .restrictions(restrictions)
                .build();
        restrictions.forEach(r -> r.setAccessibilityReason(reason));
        return reason.withRestrictions(restrictions);
    }

    @SuppressWarnings("java:S3740")
    private static List<AccessibilityRestriction> mapVehicleHeight(TrafficSign trafficSign) {

        return List.of(MaximumRestriction.builder()
                .value(trafficSign.restrictions().vehicleHeightInCm())
                .restrictionType(RestrictionType.VEHICLE_HEIGHT)
                .build());
    }

    @SuppressWarnings("java:S3740")
    private static List<AccessibilityRestriction> mapVehicleWidth(TrafficSign trafficSign) {

        return List.of(MaximumRestriction.builder()
                .value(trafficSign.restrictions().vehicleWidthInCm())
                .restrictionType(RestrictionType.VEHICLE_WIDTH)
                .build());
    }

    @SuppressWarnings("java:S3740")
    private static List<AccessibilityRestriction> mapVehicleLength(TrafficSign trafficSign) {

        return List.of(MaximumRestriction.builder()
                .value(trafficSign.restrictions().vehicleLengthInCm())
                .restrictionType(RestrictionType.VEHICLE_LENGTH)
                .build());
    }

    @SuppressWarnings("java:S3740")
    private static List<AccessibilityRestriction> mapVehicleAxleLoad(TrafficSign trafficSign) {

        return List.of(MaximumRestriction.builder()
                .value(trafficSign.restrictions().vehicleAxleLoadInKg())
                .restrictionType(RestrictionType.VEHICLE_AXLE_LOAD)
                .build());
    }

    @SuppressWarnings("java:S3740")
    private static List<AccessibilityRestriction> mapVehicleWeight(TrafficSign trafficSign) {

        return List.of(MaximumRestriction.builder()
                .value(trafficSign.restrictions().vehicleWeightInKg())
                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                .build());
    }

    @SuppressWarnings("java:S3740")
    private static List<AccessibilityRestriction> mapTransportTypes(TrafficSign trafficSign) {

        return List.of(TransportTypeRestriction.builder()
                .value(trafficSign.restrictions().transportTypes())
                .build());
    }

    @SuppressWarnings("java:S3740")
    private static List<AccessibilityRestriction> mapEmissionZoneRestrictions(TrafficSign trafficSign) {

        EmissionZoneRestriction restriction = trafficSign.restrictions().emissionZone().restriction();
        List<AccessibilityRestriction> accessibilityRestrictions = new ArrayList<>();

        if (Objects.nonNull(restriction.vehicleWeightInKg())) {
            accessibilityRestrictions.add(MaximumRestriction.builder()
                    .value(restriction.vehicleWeightInKg())
                    .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                    .build());
        }

        if(!restriction.fuelTypes().isEmpty()) {
            accessibilityRestrictions.add(FuelTypeRestriction.builder()
                    .value(restriction.fuelTypes())
                    .build());
        }

        if(!restriction.transportTypes().isEmpty()) {
            accessibilityRestrictions.add(TransportTypeRestriction.builder()
                    .value(restriction.transportTypes())
                    .build());
        }

        return accessibilityRestrictions;
    }
}
