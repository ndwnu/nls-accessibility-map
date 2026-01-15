package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.dto.Excludes;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.request.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.RoadSectionFeatureCollectionMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.validator.PointValidator;
import nu.ndw.nls.accessibilitymap.backend.exception.IncompleteArgumentsException;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.generated.api.v1.AccessibilityMapApiDelegate;
import nu.ndw.nls.accessibilitymap.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    private final PointValidator pointValidator;

    private final GraphHopperService graphHopperService;

    private final AccessibilityResponseMapper accessibilityResponseMapper;

    private final RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    private final MunicipalityService municipalityService;

    private final AccessibilityRequestMapper accessibilityRequestMapper;

    private final AccessibilityService accessibilityService;

    @Override
    public ResponseEntity<AccessibilityMapResponseJson> getInaccessibleRoadSections(
            String municipalityId,
            VehicleTypeJson vehicleType,
            Float vehicleLength,
            Float vehicleWidth,
            Float vehicleHeight,
            Float vehicleWeight,
            Float vehicleAxleLoad,
            Boolean vehicleHasTrailer,
            Double latitude,
            Double longitude,
            EmissionClassJson emissionClass,
            List<FuelTypeJson> fuelTypes,
            List<String> excludeEmissionZoneIds,
            List<EmissionZoneTypeJson> excludeEmissionZoneTypes) {

        AccessibilityRequest accessibilityRequest = buildAndValidateAccessibilityRequest(
                municipalityId, vehicleType, vehicleLength, vehicleWidth, vehicleHeight, vehicleWeight,
                vehicleAxleLoad, vehicleHasTrailer, emissionClass, fuelTypes, excludeEmissionZoneIds,
                excludeEmissionZoneTypes, latitude, longitude);

        NetworkGraphHopper networkGraphHopper = graphHopperService.getNetworkGraphHopper();

        Accessibility accessibility = accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest);

        return ResponseEntity.ok(accessibilityResponseMapper.map(accessibility));
    }

    @Override
    public ResponseEntity<RoadSectionFeatureCollectionJson> getRoadSections(
            String municipalityId,
            VehicleTypeJson vehicleType,
            Float vehicleLength,
            Float vehicleWidth,
            Float vehicleHeight,
            Float vehicleWeight,
            Float vehicleAxleLoad,
            Boolean vehicleHasTrailer,
            Boolean accessible,
            Double latitude,
            Double longitude,
            EmissionClassJson emissionClass,
            List<FuelTypeJson> fuelTypes,
            List<String> excludeEmissionZoneIds,
            List<EmissionZoneTypeJson> excludeEmissionZoneTypes) {

        AccessibilityRequest accessibilityRequest = buildAndValidateAccessibilityRequest(
                municipalityId, vehicleType, vehicleLength, vehicleWidth, vehicleHeight, vehicleWeight,
                vehicleAxleLoad, vehicleHasTrailer, emissionClass, fuelTypes, excludeEmissionZoneIds,
                excludeEmissionZoneTypes, latitude, longitude);

        NetworkGraphHopper networkGraphHopper = graphHopperService.getNetworkGraphHopper();

        Accessibility accessibility = accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest);

        return ResponseEntity.ok(
                roadSectionFeatureCollectionMapper.map(
                        accessibility.combinedAccessibility(),
                        accessibilityRequest.hasEndLocation(),
                        accessibility.toRoadSection().map(RoadSection::getId).orElse(null),
                        accessible));
    }

    @SuppressWarnings("java:S107")
    private AccessibilityRequest buildAndValidateAccessibilityRequest(
            String municipalityId,
            VehicleTypeJson vehicleType,
            Float vehicleLength,
            Float vehicleWidth,
            Float vehicleHeight,
            Float vehicleWeight,
            Float vehicleAxleLoad,
            Boolean vehicleHasTrailer,
            EmissionClassJson emissionClass,
            List<FuelTypeJson> fuelTypes,
            List<String> excludeEmissionZoneIds,
            List<EmissionZoneTypeJson> excludeEmissionZoneTypes,
            Double endPointLatitude,
            Double endPointLongitude) {

        pointValidator.validateConsistentValues(endPointLatitude, endPointLongitude);

        ensureEnvironmentalZoneParameterConsistency(emissionClass, fuelTypes);

        VehicleArguments vehicleArguments = new VehicleArguments(
                vehicleType,
                vehicleLength, vehicleWidth, vehicleHeight,
                vehicleWeight, vehicleAxleLoad,
                vehicleHasTrailer, emissionClass, fuelTypes);

        Excludes excludes = Excludes.builder()
                .emissionZoneIds(Objects.nonNull(excludeEmissionZoneIds) ? new HashSet<>(excludeEmissionZoneIds) : null)
                .emissionZoneTypes(Objects.nonNull(excludeEmissionZoneTypes) ? new HashSet<>(excludeEmissionZoneTypes) : null)
                .build();

        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);

        return accessibilityRequestMapper.map(
                municipality,
                vehicleArguments,
                excludes,
                endPointLatitude,
                endPointLongitude);
    }

    /**
     * Ensures that the parameters related to environmental zone restrictions are consistent. If one of the parameters is set and the other
     * is not, an exception is thrown.
     *
     * @param emissionClass the emission class information. Can be null, but if it is null, the fuelType must also be null.
     * @param fuelTypes     the fuel types information. Can be null, but if it is null, the emissionClass must also be null.
     * @throws IncompleteArgumentsException if only one of the parameters is set while the other is not.
     */
    @SuppressWarnings("java:S1067")
    private void ensureEnvironmentalZoneParameterConsistency(EmissionClassJson emissionClass, List<FuelTypeJson> fuelTypes) {

        if ((emissionClass == null && fuelTypes != null && !fuelTypes.isEmpty())
            || ((fuelTypes == null || fuelTypes.isEmpty()) && emissionClass != null)) {
            throw new IncompleteArgumentsException("If one of the environmental zone parameters is set, the other must be set as well.");
        }
    }
}
