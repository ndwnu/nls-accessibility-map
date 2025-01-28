package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.mappers;

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
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.BUS_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_AXLE_LOAD;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_HEIGHT;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_LENGTH;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_WEIGHT;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_WIDTH;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAILER_ACCESS_FORBIDDEN;

import java.util.Map;
import java.util.function.Function;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.dto.EdgeAttribute;
import org.springframework.stereotype.Component;

@Component
public class TrafficSignToEdgeAttributeMapper {

    private static final Map<TrafficSignType, Function<TrafficSign, Object>> VALUE_MAPPING = Map.ofEntries(
            Map.entry(C6, restrictionBooleanTrue()),
            Map.entry(C7, restrictionBooleanTrue()),
            Map.entry(C7A, restrictionBooleanTrue()),
            Map.entry(C7B, restrictionBooleanTrue()),
            Map.entry(C10, restrictionBooleanTrue()),
            Map.entry(C12, restrictionBooleanTrue()),
            Map.entry(C22C, restrictionBooleanTrue()),
            Map.entry(C17, restrictionValueBlackCode()),
            Map.entry(C18, restrictionValueBlackCode()),
            Map.entry(C19, restrictionValueBlackCode()),
            Map.entry(C20, restrictionValueBlackCode()),
            Map.entry(C21, restrictionValueBlackCode())
    );

    private static final Map<TrafficSignType, String> TRAFFIC_SIGN_KEY_MAPPING = Map.ofEntries(
            Map.entry(C6, CAR_ACCESS_FORBIDDEN),
            Map.entry(C7, HGV_ACCESS_FORBIDDEN),
            Map.entry(C7A, BUS_ACCESS_FORBIDDEN),
            Map.entry(C7B, HGV_AND_BUS_ACCESS_FORBIDDEN),
            Map.entry(C10, TRAILER_ACCESS_FORBIDDEN),
            Map.entry(C12, MOTOR_VEHICLE_ACCESS_FORBIDDEN),
            Map.entry(C22C, LCV_AND_HGV_ACCESS_FORBIDDEN),
            Map.entry(C17, MAX_LENGTH),
            Map.entry(C18, MAX_WIDTH),
            Map.entry(C19, MAX_HEIGHT),
            Map.entry(C20, MAX_AXLE_LOAD),
            Map.entry(C21, MAX_WEIGHT)

    );

    private static final Map<TrafficSignType, String> TRAFFIC_SIGN_KEY_WINDOWED_MAPPING = Map.of(
            C6, CAR_ACCESS_FORBIDDEN_WINDOWED,
            C7, HGV_ACCESS_FORBIDDEN_WINDOWED,
            C7A, HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED,
            C7B, HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED,
            C12, MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED,
            C22C, LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED);

    private static final Map<TrafficSignType, Function<TrafficSign, String>> KEY_MAPPING = Map.ofEntries(
            Map.entry(TrafficSignType.C6, getAttributeKeyWindowed()),
            Map.entry(C7, getAttributeKeyWindowed()),
            Map.entry(C7A, getAttributeKeyWindowed()),
            Map.entry(C7B, getAttributeKeyWindowed()),
            Map.entry(C10, getAttributeKey()),
            Map.entry(C12, getAttributeKeyWindowed()),
            Map.entry(C22C, getAttributeKeyWindowed()),
            Map.entry(C17, getAttributeKey()),
            Map.entry(C18, getAttributeKey()),
            Map.entry(C19, getAttributeKey()),
            Map.entry(C20, getAttributeKey()),
            Map.entry(C21, getAttributeKey())

    );

    private static Function<TrafficSign, Object> restrictionValueBlackCode() {
        return TrafficSign::blackCode;
    }

    private static Function<TrafficSign, String> getAttributeKey() {
        return trafficSign -> TRAFFIC_SIGN_KEY_MAPPING.get(trafficSign.trafficSignType());
    }

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
