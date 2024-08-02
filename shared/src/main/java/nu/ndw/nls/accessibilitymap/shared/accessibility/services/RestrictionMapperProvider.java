package nu.ndw.nls.accessibilitymap.shared.accessibility.services;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.BUS_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_AXLE_LOAD;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_HEIGHT;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_LENGTH;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_WEIGHT;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_WIDTH;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTORCYCLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.SLOW_VEHICLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRACTOR_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAILER_ACCESS_FORBIDDEN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.shared.accessibility.mappers.vehiclerestriction.MaximumRestrictionMapper;
import nu.ndw.nls.accessibilitymap.shared.accessibility.mappers.vehiclerestriction.NoEntryRestrictionMapper;
import nu.ndw.nls.accessibilitymap.shared.accessibility.mappers.vehiclerestriction.RestrictionMapper;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.VehicleProperties;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RestrictionMapperProvider {

    private static final Map<String, Predicate<VehicleProperties>> NO_ENTRY_RESTRICTION_MAP = Map.of(
            CAR_ACCESS_FORBIDDEN, VehicleProperties::carAccessForbidden,
            HGV_ACCESS_FORBIDDEN, VehicleProperties::hgvAccessForbidden,
            BUS_ACCESS_FORBIDDEN, VehicleProperties::busAccessForbidden,
            HGV_AND_BUS_ACCESS_FORBIDDEN, VehicleProperties::hgvAndBusAccessForbidden,
            TRACTOR_ACCESS_FORBIDDEN, VehicleProperties::tractorAccessForbidden,
            SLOW_VEHICLE_ACCESS_FORBIDDEN, VehicleProperties::slowVehicleAccessForbidden,
            TRAILER_ACCESS_FORBIDDEN, VehicleProperties::trailerAccessForbidden,
            MOTORCYCLE_ACCESS_FORBIDDEN, VehicleProperties::motorcycleAccessForbidden,
            MOTOR_VEHICLE_ACCESS_FORBIDDEN, VehicleProperties::motorVehicleAccessForbidden,
            LCV_AND_HGV_ACCESS_FORBIDDEN, VehicleProperties::lcvAndHgvAccessForbidden);

    private static final Map<String, Function<VehicleProperties, Double>> MAXIMUM_RESTRICTION_MAP = Map.of(
            MAX_LENGTH, VehicleProperties::length,
            MAX_WIDTH, VehicleProperties::width,
            MAX_HEIGHT, VehicleProperties::height,
            MAX_AXLE_LOAD, VehicleProperties::axleLoad,
            MAX_WEIGHT, VehicleProperties::weight);

    public List<RestrictionMapper> getMappers() {
        List<RestrictionMapper> restrictionMappers = new ArrayList<>();
        NO_ENTRY_RESTRICTION_MAP.forEach((key, vehiclePredicate) ->
                restrictionMappers.add(new NoEntryRestrictionMapper(key, vehiclePredicate)));
        MAXIMUM_RESTRICTION_MAP.forEach((key, doubleGetter) ->
                restrictionMappers.add(new MaximumRestrictionMapper(key, doubleGetter)));
        return restrictionMappers;
    }

}
