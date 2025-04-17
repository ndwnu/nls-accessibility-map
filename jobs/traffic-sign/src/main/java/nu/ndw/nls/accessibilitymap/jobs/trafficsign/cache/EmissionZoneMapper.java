package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneExemption;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EuroClassification;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.Exemption;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleType;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.service.EmissionService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmissionZoneMapper {

    private final EmissionService emissionService;

    @Valid
    public EmissionZone map(String emissionZoneId) {

        return emissionService.findById(emissionZoneId)
                .filter(nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EmissionZone::isActive)
                .map(emissionZone -> {
                    try {
                        return EmissionZone.builder()
                                .startTime(emissionZone.startTime())
                                .endTime(Objects.nonNull(emissionZone.endTime()) ? emissionZone.endTime() : OffsetDateTime.MAX)
                                .exemptions(emissionZone.exemptions().stream()
                                        .map(this::mapExemption)
                                        .collect(Collectors.toSet()))
                                .restriction(mapRestriction(emissionZone))
                                .build();
                    } catch (RuntimeException exception) {
                        log.warn("Emission zone with id '{}' is incomplete and will be skipped.", emissionZoneId, exception);
                        return null;
                    }
                })
                .orElse(null);
    }

    private EmissionZoneRestriction mapRestriction(nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EmissionZone emissionZone) {

        return EmissionZoneRestriction.builder()
                .id(emissionZone.restriction().id())
                .fuelTypes(mapFuelTypes(emissionZone.restriction().fuelType()))
                .vehicleTypes(mapVehicleTypes(emissionZone.restriction().vehicleType(),
                        emissionZone.restriction().vehicleCategories()))
                .vehicleWeightInKg(mapMaximumWeight(emissionZone.restriction().vehicleCategories()))
                .build();
    }

    private EmissionZoneExemption mapExemption(Exemption exemption) {

        return EmissionZoneExemption.builder()
                .startTime(Objects.nonNull(exemption.startTime()) ? exemption.startTime() : OffsetDateTime.MIN)
                .endTime(Objects.nonNull(exemption.endTime()) ? exemption.endTime() : OffsetDateTime.MIN)
                .transportTypes(mapVehicleTypes(exemption.vehicleCategories()))
                .vehicleWeightInKg(mapMaximumWeight(exemption.vehicleCategories(), mapToDouble(exemption.vehicleWeightInKg())))
                .emissionClasses(mapEmissionClassification(exemption.euroClassifications()))
                .build();
    }

    private Double mapToDouble(Integer value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return Double.valueOf(value);
    }

    private Set<EmissionClass> mapEmissionClassification(Set<EuroClassification> euroClassifications) {

        return euroClassifications.stream()
                .map(euroClassification -> switch (euroClassification) {
                    case EURO_1 -> EmissionClass.ONE;
                    case EURO_2 -> EmissionClass.TWO;
                    case EURO_3 -> EmissionClass.THREE;
                    case EURO_4 -> EmissionClass.FOUR;
                    case EURO_5 -> EmissionClass.FIVE;
                    case EURO_6 -> EmissionClass.SIX;
                    case UNKNOWN -> throw new IllegalStateException("Unknown euro classification '%s'.".formatted(euroClassification));
                })
                .collect(Collectors.toSet());
    }

    private Maximum mapMaximumWeight(Set<VehicleCategory> vehicleCategories, Double maximumWeightInKg) {

        Maximum vehicleCategoriesMaximumWeightInKg = mapMaximumWeight(vehicleCategories);
        if (Objects.isNull(vehicleCategoriesMaximumWeightInKg) && Objects.isNull(maximumWeightInKg)) {
            return null;
        }
        if (Objects.nonNull(vehicleCategoriesMaximumWeightInKg) && Objects.isNull(maximumWeightInKg)) {
            return vehicleCategoriesMaximumWeightInKg;
        }

        if (Objects.isNull(vehicleCategoriesMaximumWeightInKg)) {
            return Maximum.builder().value(maximumWeightInKg).build();
        }

        if (vehicleCategoriesMaximumWeightInKg.value() <= maximumWeightInKg) {
            return vehicleCategoriesMaximumWeightInKg;
        } else {
            return Maximum.builder().value(maximumWeightInKg).build();
        }
    }

    private Maximum mapMaximumWeight(Set<VehicleCategory> vehicleCategories) {

        if (Objects.isNull(vehicleCategories)) {
            return null;
        }

        return vehicleCategories.stream()
                .map(vehicleCategory ->
                        switch (vehicleCategory) {
                            case M, N, M_1 -> null;
                            case M_2 -> 5_000D;
                            case N_1 -> 35_000D;
                            case N_2 -> 12_000D;
                            case M_3, N_3 -> Double.MAX_VALUE;
                            case UNKNOWN -> throw new IllegalStateException("Unknown vehicle category '%s'.".formatted(vehicleCategory));
                        })
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .map(minimalWeightRestriction -> Maximum.builder()
                        .value(minimalWeightRestriction)
                        .build())
                .orElse(null);
    }

    private Set<TransportType> mapVehicleTypes(VehicleType vehicleType, Set<VehicleCategory> vehicleCategories) {

        var vehicleTypes = mapVehicleTypes(vehicleType);

        var vehicleTypesFromCategories = mapVehicleTypes(vehicleCategories);

        return Stream.concat(vehicleTypes.stream(), vehicleTypesFromCategories.stream()).collect(Collectors.toSet());

    }

    private static Set<TransportType> mapVehicleTypes(VehicleType vehicleType) {

        if (vehicleType == null) {
            return Set.of();
        }

        return switch (vehicleType) {
            case AGRICULTURAL_VEHICLE -> Set.of(TransportType.TRACTOR);
            case ANY_VEHICLE -> Set.of(TransportType.values());
            case BICYCLE -> Set.of(TransportType.BICYCLE);
            case BUS -> Set.of(TransportType.BUS);
            case CAR -> Set.of(TransportType.CAR);
            case CAR_WITH_CARAVAN -> Set.of(TransportType.CARAVAN);
            case CAR_WITH_TRAILER -> Set.of(TransportType.CAR, TransportType.VEHICLE_WITH_TRAILER);
            case LORRY, VAN -> Set.of(TransportType.DELIVERY_VAN);
            case MOPED -> Set.of(TransportType.MOPED);
            case MOTORCYCLE -> Set.of(TransportType.MOTORCYCLE);
            case MOTORSCOOTER -> Set.of(TransportType.MOTORSCOOTER);
            case VEHICLE_WITH_TRAILER -> Set.of(TransportType.VEHICLE_WITH_TRAILER);
            case ARROW_BOARD_VEHICLE, CONSTRUCTION_OR_MAINTENANCE_VEHICLE, CRASH_DAMPENING_VEHICLE, MOBILE_VARIABLE_MESSAGE_SIGN_VEHICLE,
                 MOBILE_LANE_SIGNALING_VEHICLE -> Set.of();
            case UNKNOWN -> throw new IllegalStateException("Unknown vehicle type '%s'.".formatted(vehicleType));
        };
    }

    private Set<TransportType> mapVehicleTypes(Set<VehicleCategory> vehicleCategories) {
        if (Objects.isNull(vehicleCategories)) {
            return Set.of();
        }
        return vehicleCategories.stream()
                .map(vehicleCategory ->
                        switch (vehicleCategory) {
                            case M, M_1, M_2, M_3 ->
                                    Set.of(TransportType.BUS, TransportType.CAR, TransportType.TAXI, TransportType.CARAVAN);
                            case N, N_1, N_2, N_3 -> Set.of(TransportType.TRUCK, TransportType.DELIVERY_VAN);
                            case UNKNOWN -> throw new IllegalStateException("Unknown vehicle category '%s'.".formatted(vehicleCategory));
                        })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<FuelType> mapFuelTypes(nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.FuelType fuelType) {

        if (Objects.isNull(fuelType)) {
            return Set.of();
        }

        return switch (fuelType) {
            case ALL -> Set.of(FuelType.values());
            case BATTERY -> Set.of(FuelType.ELECTRIC);
            case BIODIESEL -> Set.of(FuelType.BIODIESEL);
            case DIESEL -> Set.of(FuelType.DIESEL);
            case DIESEL_BATTERY_HYBRID -> Set.of(FuelType.DIESEL, FuelType.ELECTRIC);
            case HYDROGEN -> Set.of(FuelType.HYDROGEN);
            case LPG, LIQUID_GAS -> Set.of(FuelType.LPG);
            case METHANE -> Set.of(FuelType.METHANE);
            case PETROL, PETROL_UNLEADED, PETROL_LEADED, PETROL_98_OCTANE, PETROL_95_OCTANE -> Set.of(FuelType.PETROL);
            case PETROL_BATTERY_HYBRID -> Set.of(FuelType.PETROL, FuelType.ELECTRIC);
            case ETHANOL, OTHER, UNKNOWN -> Set.of(FuelType.UNKNOWN);
        };
    }
}
