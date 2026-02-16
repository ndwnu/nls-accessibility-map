package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.FuelTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.TransportTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper.AccessibilityRequestBuilderAreaMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ExclusionsJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.LocationJson;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityRequestMapperV2 {

    private static final int MULTIPLIER_FROM_METERS_TO_CM = 100;

    private static final int MULTIPLIER_FROM_TONNE_TO_KILO_GRAM = 1000;

    private final TransportTypeMapperV2 transportTypeMapperV2;

    private final EmissionClassMapperV2 emissionClassMapperV2;

    private final FuelTypeMapperV2 fuelTypeMapperV2;

    private final EmissionZoneTypeMapperV2 emissionZoneTypeMapperV2;

    private final ClockService clockService;

    private final AccessibilityRequestRestrictionMapper accessibilityRequestRestrictionMapper;

    private final List<AccessibilityRequestBuilderAreaMapper> areaRequestMappers;

    @Valid
    public AccessibilityRequest map(NetworkData networkData, AccessibilityRequestJson accessibilityRequest) {

        AccessibilityRequestBuilder accessibilityRequestBuilder = AccessibilityRequest.builder();

        mapFrom(accessibilityRequest, accessibilityRequestBuilder);
        mapDestination(accessibilityRequest, accessibilityRequestBuilder);
        mapAreaRequest(accessibilityRequest, accessibilityRequestBuilder);

        accessibilityRequestBuilder
                .timestamp(clockService.now())
                .addMissingRoadsSectionsFromNwb(true)
                .vehicleHeightInCm(mapToDouble(accessibilityRequest.getVehicle().getHeight(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleLengthInCm(mapToDouble(accessibilityRequest.getVehicle().getLength(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleWidthInCm(mapToDouble(accessibilityRequest.getVehicle().getWidth(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleWeightInKg(mapToDouble(accessibilityRequest.getVehicle().getWeight(), MULTIPLIER_FROM_TONNE_TO_KILO_GRAM))
                .vehicleAxleLoadInKg(mapToDouble(accessibilityRequest.getVehicle().getAxleLoad(), MULTIPLIER_FROM_TONNE_TO_KILO_GRAM))
                .fuelTypes(
                        Objects.isNull(accessibilityRequest.getVehicle().getFuelTypes())
                                ? null
                                : accessibilityRequest.getVehicle().getFuelTypes().stream()
                                        .map(fuelTypeMapperV2::map)
                                        .collect(Collectors.toSet()))
                .emissionClasses(emissionClassMapperV2.map(accessibilityRequest.getVehicle().getEmissionClass()))
                .transportTypes(transportTypeMapperV2.map(accessibilityRequest.getVehicle()))
                .excludeRestrictionsWithEmissionZoneIds(mapEmissionZoneIds(accessibilityRequest.getExclusions()))
                .excludeRestrictionsWithEmissionZoneTypes(mapEmissionZoneTypes(accessibilityRequest.getExclusions()))
                .dynamicRestrictions(mapRestrictions(accessibilityRequest.getRestrictions(), networkData));

        return accessibilityRequestBuilder.build();
    }

    private static void mapDestination(
            AccessibilityRequestJson accessibilityRequest,
            AccessibilityRequestBuilder accessibilityRequestBuilder) {
        LocationJson destination = accessibilityRequest.getDestination();
        if (Objects.nonNull(destination)) {
            accessibilityRequestBuilder
                    .endLocationLatitude(destination.getLatitude())
                    .endLocationLongitude(destination.getLongitude());
        }
    }

    private static void mapFrom(
            AccessibilityRequestJson accessibilityRequestJson,
            AccessibilityRequestBuilder accessibilityRequestBuilder) {
        LocationJson from = accessibilityRequestJson.getFrom();
        if (Objects.nonNull(from)) {
            accessibilityRequestBuilder
                    .startLocationLatitude(from.getLatitude())
                    .startLocationLongitude(from.getLongitude());
        }
    }

    private void mapAreaRequest(
            AccessibilityRequestJson accessibilityRequestJson,
            AccessibilityRequestBuilder accessibilityRequestBuilder) {

        areaRequestMappers.stream()
                .filter(mapper -> mapper.canProcessAreaRequest(accessibilityRequestJson.getArea()))
                .findFirst()
                .ifPresentOrElse(
                        mapper -> mapper.build(accessibilityRequestBuilder, accessibilityRequestJson.getArea()),
                        () -> {
                            throw new ApiException(
                                    UUID.fromString("fdd86f4f-a34d-4a01-8abc-4ea8b0e327a9"),
                                    HttpStatus.BAD_REQUEST,
                                    "Invalid area type",
                                    "Area type of '%s' is not a valid. Please check the api specification for valid options."
                                            .formatted(accessibilityRequestJson.getArea().getClass().getSimpleName()));
                        });
    }

    private Set<Restriction> mapRestrictions(
            List<AccessibilityRequestRestrictionJson> restrictions,
            NetworkData networkData) {

        if (Objects.isNull(restrictions)) {
            return Set.of();
        }

        return restrictions.stream()
                .map(accessibilityRequestRestrictionJson ->
                        accessibilityRequestRestrictionMapper.map(networkData, accessibilityRequestRestrictionJson))
                .collect(Collectors.toSet());
    }

    private static Set<String> mapEmissionZoneIds(ExclusionsJson exclusions) {

        return Objects.isNull(exclusions) || Objects.isNull(exclusions.getEmissionZoneIds())
                ? null
                : new HashSet<>(exclusions.getEmissionZoneIds());
    }

    private Set<EmissionZoneType> mapEmissionZoneTypes(ExclusionsJson exclusions) {

        return Objects.isNull(exclusions) || Objects.isNull(exclusions.getEmissionZoneTypes())
                ? null
                : exclusions.getEmissionZoneTypes().stream()
                        .map(emissionZoneTypeMapperV2::map)
                        .collect(Collectors.toSet());
    }

    private static Double mapToDouble(Float value, int multiplier) {

        return Objects.nonNull(value) ? (Double.valueOf(value) * multiplier) : null;
    }
}
