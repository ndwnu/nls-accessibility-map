package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C10;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C12;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C17;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C18;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C19;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C20;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C21;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C22C;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C6;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7A;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7B;

import java.util.Map;
import java.util.function.BiFunction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties.VehiclePropertiesBuilder;
import org.springframework.stereotype.Component;

@Component
public class VehiclePropertiesMapper {

    private static final double MAX_LENGTH = 22D;

    private static final double MAX_HEIGHT = 4D;

    private static final double MAX_AXLE_LOAD = 12D;

    private static final double MAX_WEIGHT = 60D;

    private static final double MAX_WIDTH = 3D;

    private static final Map<TrafficSignType, BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder>> MAPPINGS =
            Map.ofEntries(
                    Map.entry(C1, setC1Forbidden()),
                    Map.entry(C6, setCarAccessForbidden()),
                    Map.entry(C7, setHgvAccessForBidden()),
                    Map.entry(C7A, setBusAccessForbidden()),
                    Map.entry(C7B, setHgvAndBusAccessForbidden()),
                    Map.entry(C10, setTrailerAccessForbidden()),
                    Map.entry(C12, setMotorVehicleAccessForbidden()),
                    Map.entry(C22C, setLcvAndHgvAccessForbidden()),
                    Map.entry(C17, setLength()),
                    Map.entry(C18, setWidth()),
                    Map.entry(C19, setHeight()),
                    Map.entry(C20, setAxleLoad()),
                    Map.entry(C21, setWeight()));

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setWeight() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.weight(MAX_WEIGHT);
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setAxleLoad() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.axleLoad(MAX_AXLE_LOAD);
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setLength() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.length(MAX_LENGTH);
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setWidth() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.width(MAX_WIDTH);
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setHeight() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.height(MAX_HEIGHT);
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setLcvAndHgvAccessForbidden() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.lcvAndHgvAccessForbiddenWt(true);
            if (Boolean.FALSE.equals(includeOnlyWindowTimes)) {
                builder.lcvAndHgvAccessForbidden(true);
            }
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setMotorVehicleAccessForbidden() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.motorVehicleAccessForbiddenWt(true);
            if (Boolean.FALSE.equals(includeOnlyWindowTimes)) {
                builder.motorVehicleAccessForbidden(true);
            }
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setTrailerAccessForbidden() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.trailerAccessForbidden(true);
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setC1Forbidden() {
        return (builder, includeOnlyWindowTimes) -> {
            setCarAccessForbidden().apply(builder, includeOnlyWindowTimes);
            setHgvAccessForBidden().apply(builder, includeOnlyWindowTimes);
            setBusAccessForbidden().apply(builder, includeOnlyWindowTimes);
            setHgvAndBusAccessForbidden().apply(builder, includeOnlyWindowTimes);
            setTrailerAccessForbidden().apply(builder, includeOnlyWindowTimes);
            setMotorVehicleAccessForbidden().apply(builder, includeOnlyWindowTimes);
            setLcvAndHgvAccessForbidden().apply(builder, includeOnlyWindowTimes);
            setLcvAndHgvAccessForbidden().apply(builder, includeOnlyWindowTimes);
            builder.tractorAccessForbidden(true);
            builder.motorcycleAccessForbidden(true);
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setCarAccessForbidden() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.carAccessForbiddenWt(true);
            if (Boolean.FALSE.equals(includeOnlyWindowTimes)) {
                builder.carAccessForbidden(true);
            }
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setHgvAccessForBidden() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.hgvAccessForbiddenWt(true);
            if (Boolean.FALSE.equals(includeOnlyWindowTimes)) {
                builder.hgvAccessForbidden(true);
            }
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setHgvAndBusAccessForbidden() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.hgvAndBusAccessForbiddenWt(true);
            if (Boolean.FALSE.equals(includeOnlyWindowTimes)) {
                builder.hgvAndBusAccessForbidden(true);
            }
            return builder;
        };
    }

    private static BiFunction<VehiclePropertiesBuilder, Boolean, VehiclePropertiesBuilder> setBusAccessForbidden() {
        return (builder, includeOnlyWindowTimes) -> {
            builder.busAccessForbidden(true);
            return builder;
        };
    }

    public VehicleProperties map(Iterable<TrafficSignType> trafficSignTypes, boolean includeOnlyWindowTimes) {

        VehiclePropertiesBuilder vehiclePropertiesBuilder = VehicleProperties.builder();
        trafficSignTypes.forEach(trafficSignType -> {
            if (!MAPPINGS.containsKey(trafficSignType)) {
                throw new IllegalArgumentException(
                        "TrafficSignType is not defined and therefore vehicleProperties could not be created.");
            }
            MAPPINGS.get(trafficSignType).apply(vehiclePropertiesBuilder, includeOnlyWindowTimes);
        });

        return vehiclePropertiesBuilder.build();
    }
}
