package nu.ndw.nls.accessibilitymap.job.trafficsign.cache;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.EmissionZoneMapper;
import org.springframework.stereotype.Service;

@Service
public class TrafficSignRestrictionsBuilder {

    private static final int MULTIPLIER_FROM_METERS_TO_CM = 100;

    private static final int MULTIPLIER_FROM_TONNE_TO_KILO_GRAM = 1000;

    private final Map<TrafficSignType, TransportRestrictions> nonDynamicTrafficSigns;

    private final Map<TrafficSignType, Function<TrafficSign, TransportRestrictions>> dynamicTrafficSigns;

    private final EmissionZoneMapper emissionZoneMapper;

    public TrafficSignRestrictionsBuilder(EmissionZoneMapper emissionZoneMapper) {

        this.emissionZoneMapper = emissionZoneMapper;

        nonDynamicTrafficSigns = new EnumMap<>(TrafficSignType.class);
        nonDynamicTrafficSigns.put(TrafficSignType.C1, buildC1Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C6, buildC6Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7, buildC7Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7A, buildC7aRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7B, buildC7bRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C8, buildC8Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C9, buildC9Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C10, buildC10Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C11, buildC11Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C12, buildC12Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C22, buildC22Restrictions());

        dynamicTrafficSigns = new EnumMap<>(TrafficSignType.class);
        dynamicTrafficSigns.put(TrafficSignType.C17, buildC17Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C18, buildC18Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C19, buildC19Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C20, buildC20Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C21, buildC21Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C22A, buildEmissionZoneRestrictions());
        dynamicTrafficSigns.put(TrafficSignType.C22C, buildEmissionZoneRestrictions());
    }

    public TransportRestrictions buildFor(TrafficSign trafficSign) {

        if (!nonDynamicTrafficSigns.containsKey(trafficSign.trafficSignType())) {
            if (!dynamicTrafficSigns.containsKey(trafficSign.trafficSignType())) {
                throw new IllegalArgumentException("Traffic sign type " + trafficSign.trafficSignType() + " is not supported");
            }

            return dynamicTrafficSigns.get(trafficSign.trafficSignType()).apply(trafficSign);
        }

        return nonDynamicTrafficSigns.get(trafficSign.trafficSignType());
    }

    private static TransportRestrictions buildC1Restrictions() {
        return TransportRestrictions.builder()
                .transportTypes(TransportType.allExcept(TransportType.PEDESTRIAN))
                .build();
    }

    private static TransportRestrictions buildC9Restrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(
                        TransportType.TRACTOR,
                        TransportType.MOTORCYCLE,
                        TransportType.BICYCLE,
                        TransportType.RIDERS
                ))
                .build();
    }

    private static TransportRestrictions buildC8Restrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(TransportType.TRACTOR))
                .build();
    }

    private static TransportRestrictions buildC6Restrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(
                        TransportType.BUS,
                        TransportType.CAR,
                        TransportType.DELIVERY_VAN,
                        TransportType.TAXI,
                        TransportType.TRACTOR,
                        TransportType.TRUCK
                ))
                .build();
    }

    private static TransportRestrictions buildC7Restrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(TransportType.TRUCK))
                .build();
    }

    private static TransportRestrictions buildC7aRestrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(TransportType.BUS))
                .build();
    }

    private static TransportRestrictions buildC7bRestrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(TransportType.BUS, TransportType.TRUCK))
                .build();
    }

    private static TransportRestrictions buildC10Restrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(TransportType.VEHICLE_WITH_TRAILER))
                .build();
    }

    private static TransportRestrictions buildC11Restrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(TransportType.MOTORCYCLE))
                .build();
    }

    private static TransportRestrictions buildC12Restrictions() {
        return TransportRestrictions.builder()
                .transportTypes(Set.of(
                        TransportType.BUS,
                        TransportType.CAR,
                        TransportType.DELIVERY_VAN,
                        TransportType.MOPED,
                        TransportType.MOTORCYCLE,
                        TransportType.TAXI,
                        TransportType.TRACTOR,
                        TransportType.TRUCK
                ))
                .build();
    }

    private static Function<TrafficSign, TransportRestrictions> buildC17Restrictions() {
        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return TransportRestrictions.builder().build();
            }
            return TransportRestrictions.builder()
                    .vehicleLengthInCm(Maximum.builder()
                            .value(trafficSign.blackCode() * MULTIPLIER_FROM_METERS_TO_CM)
                            .build())
                    .build();
        };
    }

    private static Function<TrafficSign, TransportRestrictions> buildC18Restrictions() {
        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return TransportRestrictions.builder().build();
            }
            return TransportRestrictions.builder()
                    .vehicleWidthInCm(Maximum.builder()
                            .value(trafficSign.blackCode() * MULTIPLIER_FROM_METERS_TO_CM)
                            .build())
                    .build();
        };
    }

    private static Function<TrafficSign, TransportRestrictions> buildC19Restrictions() {
        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return TransportRestrictions.builder().build();
            }
            return TransportRestrictions.builder()
                    .vehicleHeightInCm(Maximum.builder()
                            .value(trafficSign.blackCode() * MULTIPLIER_FROM_METERS_TO_CM)
                            .build())
                    .build();
        };
    }

    private static Function<TrafficSign, TransportRestrictions> buildC20Restrictions() {

        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return TransportRestrictions.builder().build();
            }
            return TransportRestrictions.builder()
                    .vehicleAxleLoadInKg(Maximum.builder()
                            .value(trafficSign.blackCode() * MULTIPLIER_FROM_TONNE_TO_KILO_GRAM)
                            .build())
                    .build();
        };
    }

    private static Function<TrafficSign, TransportRestrictions> buildC21Restrictions() {

        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return TransportRestrictions.builder().build();
            }
            return TransportRestrictions.builder()
                    .vehicleWeightInKg(Maximum.builder()
                            .value(trafficSign.blackCode() * MULTIPLIER_FROM_TONNE_TO_KILO_GRAM)
                            .build())
                    .build();
        };
    }

    private static TransportRestrictions buildC22Restrictions() {

        return TransportRestrictions.builder()
                .transportTypes(Set.of(TransportType.VEHICLE_WITH_DANGEROUS_SUPPLIES))
                .build();
    }

    private Function<TrafficSign, TransportRestrictions> buildEmissionZoneRestrictions() {

        return trafficSign -> TransportRestrictions.builder()
                .emissionZone(emissionZoneMapper.map(trafficSign.trafficRegulationOrderId()))
                .build();
    }
}
