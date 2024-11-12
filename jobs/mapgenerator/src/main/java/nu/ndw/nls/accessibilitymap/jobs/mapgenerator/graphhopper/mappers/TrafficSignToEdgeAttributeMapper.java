package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.mappers;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED;

import java.util.Map;
import java.util.function.Function;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.dto.EdgeAttribute;
import org.springframework.stereotype.Component;

@Component
public class TrafficSignToEdgeAttributeMapper {

    private static final Map<TrafficSignType, Function<TrafficSign, Object>> VALUE_MAPPING = Map.of(
            TrafficSignType.C6, restrictionBooleanTrue(),
            TrafficSignType.C7, restrictionBooleanTrue(),
            TrafficSignType.C7B, restrictionBooleanTrue(),
            TrafficSignType.C12, restrictionBooleanTrue(),
            TrafficSignType.C22C, restrictionBooleanTrue()
    );

    private static final Map<TrafficSignType, String> TRAFFIC_SIGN_KEY_MAPPING = Map.of(
            TrafficSignType.C6, CAR_ACCESS_FORBIDDEN,
            TrafficSignType.C7, HGV_ACCESS_FORBIDDEN,
            TrafficSignType.C7B, HGV_AND_BUS_ACCESS_FORBIDDEN,
            TrafficSignType.C12, MOTOR_VEHICLE_ACCESS_FORBIDDEN,
            TrafficSignType.C22C, LCV_AND_HGV_ACCESS_FORBIDDEN);

    private static final Map<TrafficSignType, String> TRAFFIC_SIGN_KEY_WINDOWED_MAPPING = Map.of(
            TrafficSignType.C6, CAR_ACCESS_FORBIDDEN_WINDOWED,
            TrafficSignType.C7, HGV_ACCESS_FORBIDDEN_WINDOWED,
            TrafficSignType.C7B, HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED,
            TrafficSignType.C12, MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED,
            TrafficSignType.C22C, LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED);

    private static final Map<TrafficSignType, Function<TrafficSign, String>> KEY_MAPPING = Map.of(
            TrafficSignType.C6, getAttributeKeyWindowed(),
            TrafficSignType.C7, getAttributeKeyWindowed(),
            TrafficSignType.C7B, getAttributeKeyWindowed(),
            TrafficSignType.C12, getAttributeKeyWindowed(),
            TrafficSignType.C22C, getAttributeKeyWindowed()
    );

    private static Function<TrafficSign, String> getAttributeKeyWindowed() {
        return trafficSign -> trafficSign.hasTimeWindowedSign()
                ? TRAFFIC_SIGN_KEY_WINDOWED_MAPPING.get(trafficSign.trafficSignType())
                : TRAFFIC_SIGN_KEY_MAPPING.get(trafficSign.trafficSignType());
    }

    private static Function<TrafficSign, Object> restrictionBooleanTrue() {
        return trafficSign -> true;
    }

    public EdgeAttribute mapToEdgeAttribute(TrafficSign trafficSign) {
        if (hasMapping(trafficSign)) {
            return EdgeAttribute.builder()
                    .key(KEY_MAPPING.get(trafficSign.trafficSignType()).apply(trafficSign))
                    .value(VALUE_MAPPING.get(trafficSign.trafficSignType()).apply(trafficSign))
                    .build();
        } else {
            throw new IllegalArgumentException("TrafficSign " + trafficSign + " is not supported");
        }
    }

    private static boolean hasMapping(TrafficSign trafficSign) {
        return KEY_MAPPING.containsKey(trafficSign.trafficSignType())
                && VALUE_MAPPING.containsKey(trafficSign.trafficSignType());
    }
}
