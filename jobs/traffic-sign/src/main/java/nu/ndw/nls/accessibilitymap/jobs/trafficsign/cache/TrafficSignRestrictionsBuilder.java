package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.springframework.stereotype.Service;

@Service
public class TrafficSignRestrictionsBuilder {

    private final Map<TrafficSignType, Restrictions> nonDynamicTrafficSigns;

    private final Map<TrafficSignType, Function<TrafficSign, Restrictions>> dynamicTrafficSigns;

    public TrafficSignRestrictionsBuilder() {

        nonDynamicTrafficSigns = new EnumMap<>(TrafficSignType.class);
        nonDynamicTrafficSigns.put(TrafficSignType.C1, buildC1Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C6, buildC6Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7, buildC7Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7A, buildC7aRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7B, buildC7bRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7C, buildC7cRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C8, buildC8Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C9, buildC9Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C10, buildC10Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C11, buildC11Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C12, buildC12Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C22, buildC22Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C22C, buildC22cRestrictions());

        dynamicTrafficSigns = new EnumMap<>(TrafficSignType.class);
        dynamicTrafficSigns.put(TrafficSignType.C17, buildC17Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C18, buildC18Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C19, buildC19Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C20, buildC20Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C21, buildC21Restrictions());
    }

    public Restrictions buildFor(TrafficSign trafficSign) {

        if (!nonDynamicTrafficSigns.containsKey(trafficSign.trafficSignType())) {
            if (!dynamicTrafficSigns.containsKey(trafficSign.trafficSignType())) {
                throw new IllegalArgumentException("Traffic sign type " + trafficSign.trafficSignType() + " is not supported");
            }

            return dynamicTrafficSigns.get(trafficSign.trafficSignType()).apply(trafficSign);
        }

        return nonDynamicTrafficSigns.get(trafficSign.trafficSignType());
    }

    private static Restrictions buildC1Restrictions() {
        return Restrictions.builder()
                .transportTypes(TransportType.allExcept(TransportType.PEDESTRIAN))
                .build();
    }

    private static Restrictions buildC9Restrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(
                        TransportType.TRACTOR,
                        TransportType.MOTORCYCLE,
                        TransportType.BICYCLE,
                        TransportType.RIDERS
                ))
                .build();
    }

    private static Restrictions buildC8Restrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.TRACTOR))
                .build();
    }

    private static Restrictions buildC6Restrictions() {
        return Restrictions.builder()
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

    private static Restrictions buildC7Restrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.TRUCK))
                .build();
    }

    private static Restrictions buildC7aRestrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.BUS))
                .build();
    }

    private static Restrictions buildC7bRestrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.BUS, TransportType.TRUCK))
                .build();
    }

    private static Restrictions buildC7cRestrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.DELIVERY_VAN, TransportType.TRUCK))
                .build();
    }

    private static Restrictions buildC10Restrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.VEHICLE_WITH_TRAILER))
                .build();
    }

    private static Restrictions buildC11Restrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.MOTORCYCLE))
                .build();
    }

    private static Restrictions buildC12Restrictions() {
        return Restrictions.builder()
                .transportTypes(Set.of(
                        TransportType.MOTORCYCLE,
                        TransportType.BUS,
                        TransportType.CAR,
                        TransportType.DELIVERY_VAN,
                        TransportType.TAXI,
                        TransportType.TRACTOR,
                        TransportType.TRUCK
                ))
                .build();
    }

    private static Function<TrafficSign, Restrictions> buildC17Restrictions() {
        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return Restrictions.builder().build();
            }
            return Restrictions.builder()
                    .vehicleLengthInCm(Maximum.builder()
                            .value(trafficSign.blackCode())
                            .build())
                    .build();
        };
    }

    private static Function<TrafficSign, Restrictions> buildC18Restrictions() {
        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return Restrictions.builder().build();
            }
            return Restrictions.builder()
                    .vehicleWidthInCm(Maximum.builder()
                            .value(trafficSign.blackCode())
                            .build())
                    .build();
        };
    }

    private static Function<TrafficSign, Restrictions> buildC19Restrictions() {
        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return Restrictions.builder().build();
            }
            return Restrictions.builder()
                    .vehicleHeightInCm(Maximum.builder()
                            .value(trafficSign.blackCode())
                            .build())
                    .build();
        };
    }

    private static Function<TrafficSign, Restrictions> buildC20Restrictions() {

        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return Restrictions.builder().build();
            }
            return Restrictions.builder()
                    .vehicleAxleLoadInKg(Maximum.builder()
                            .value(trafficSign.blackCode())
                            .build())
                    .build();
        };
    }

    private static Function<TrafficSign, Restrictions> buildC21Restrictions() {

        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return Restrictions.builder().build();
            }
            return Restrictions.builder()
                    .vehicleWeightInKg(Maximum.builder()
                            .value(trafficSign.blackCode())
                            .build())
                    .build();
        };
    }

    private static Restrictions buildC22Restrictions() {

        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.VEHICLE_WITH_DANGEROUS_SUPPLIES))
                .build();
    }

    private static Restrictions buildC22cRestrictions() {

        return Restrictions.builder()
                .transportTypes(Set.of(TransportType.DELIVERY_VAN, TransportType.TRUCK))
                .build();
    }
}
