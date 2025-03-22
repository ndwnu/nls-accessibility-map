package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value.Range;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value.TransportType;
import org.springframework.stereotype.Service;

@Service
public class TrafficSignRestrictionsMapper {

    private final Map<TrafficSignType, Restrictions> nonDynamicTrafficSigns;

    private final Map<TrafficSignType, Function<TrafficSign, Restrictions>> dynamicTrafficSigns;

    public TrafficSignRestrictionsMapper() {

        nonDynamicTrafficSigns = new EnumMap<>(TrafficSignType.class);
        nonDynamicTrafficSigns.put(TrafficSignType.C1, buildC1Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7, buildC7Restrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7A, buildC7aRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7B, buildC7bRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7C, buildC7cRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C7B, buildC7bRestrictions());
        nonDynamicTrafficSigns.put(TrafficSignType.C22, buildC7cRestrictions());

        dynamicTrafficSigns = new EnumMap<>(TrafficSignType.class);
        dynamicTrafficSigns.put(TrafficSignType.C17, buildC17Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C18, buildC18Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C19, buildC19Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C20, buildC20Restrictions());
        dynamicTrafficSigns.put(TrafficSignType.C21, buildC21Restrictions());
    }

    public Restrictions map(TrafficSignType trafficSignType, TrafficSign trafficSign) {

        if (!nonDynamicTrafficSigns.containsKey(trafficSignType)) {
            if (!dynamicTrafficSigns.containsKey(trafficSignType)) {

                throw new IllegalArgumentException("Traffic sign type " + trafficSignType + " is not supported");
            }

            return dynamicTrafficSigns.get(trafficSignType).apply(trafficSign);
        }

        return nonDynamicTrafficSigns.get(trafficSignType);
    }

    private static Restrictions buildC1Restrictions() {
        return Restrictions.builder()
                .isBlocked(true)
                .build();
    }

    private static Restrictions buildC7Restrictions() {
        return Restrictions.builder()
                .transportTypes(List.of(TransportType.TRUCK))
                .build();
    }

    private static Restrictions buildC7aRestrictions() {
        return Restrictions.builder()
                .transportTypes(List.of(TransportType.BUS))
                .build();
    }

    private static Restrictions buildC7bRestrictions() {
        return Restrictions.builder()
                .transportTypes(List.of(TransportType.TRUCK, TransportType.BUS))
                .build();
    }

    private static Restrictions buildC7cRestrictions() {
        return Restrictions.builder()
                .transportTypes(List.of(TransportType.TRUCK, TransportType.DELIVERY_VAN))
                .build();
    }

    private static Function<TrafficSign, Restrictions> buildC17Restrictions() {
        return trafficSign -> {
            if (Objects.isNull(trafficSign.blackCode()) || trafficSign.blackCode() == 0) {
                return Restrictions.builder().build();
            }
            return Restrictions.builder()
                    .vehicleLength(Range.builder()
                            .min(trafficSign.blackCode())
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
                    .vehicleWidth(Range.builder()
                            .min(trafficSign.blackCode())
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
                    .vehicleHeight(Range.builder()
                            .min(trafficSign.blackCode())
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
                    .vehicleWeight(Range.builder()
                            .min(trafficSign.blackCode())
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
                    .vehicleAxleLoad(Range.builder()
                            .min(trafficSign.blackCode())
                            .build())
                    .build();
        };
    }
}
