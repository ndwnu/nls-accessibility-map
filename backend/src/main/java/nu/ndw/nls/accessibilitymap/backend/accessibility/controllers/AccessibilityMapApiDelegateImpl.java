package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibleRoadSectionModifier;
import nu.ndw.nls.accessibilitymap.accessibility.services.MissingRoadSectionProvider;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response.PointMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response.RoadSectionFeatureCollectionMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.validators.PointValidator;
import nu.ndw.nls.accessibilitymap.backend.accessibility.service.PointMatchService;
import nu.ndw.nls.accessibilitymap.backend.exceptions.IncompleteArgumentsException;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.AccessibilityMapApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.services.MunicipalityService;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    private final PointValidator pointValidator;

    private final PointMapper pointMapper;

    private final PointMatchService pointMatchService;

    private final AccessibilityResponseMapper accessibilityResponseMapper;

    private final RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionV2Mapper;

    private final MunicipalityService municipalityService;

    private final AccessibilityRequestMapper accessibilityRequestV2Mapper;

    private final AccessibilityService accessibilityService;

    private final MissingRoadSectionProvider missingRoadSectionProvider;

    private final ClockService clockService;

    @Override
    public ResponseEntity<AccessibilityMapResponseJson> getInaccessibleRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer, Double latitude, Double longitude,
            EmissionClassJson emissionClass,
            FuelTypeJson fuelType) {
        ensureEnvironmentalZoneParameterConsistency(emissionClass, fuelType);
        Integer requestedRoadSectionId = mapStartPoint(latitude, longitude)
                .flatMap(this::matchStartPoint)
                .map(CandidateMatch::getMatchedLinkId)
                .orElse(null);

        VehicleArguments requestArguments = new VehicleArguments(
                vehicleType,
                vehicleLength, vehicleWidth, vehicleHeight,
                vehicleWeight, vehicleAxleLoad,
                vehicleHasTrailer, emissionClass, fuelType);

        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);
        AccessibilityRequest accessibilityRequest = accessibilityRequestV2Mapper.mapToAccessibilityRequest(clockService.now(), municipality,
                requestArguments);

        Accessibility accessibility = accessibilityService.calculateAccessibility(
                accessibilityRequest,
                addMissingRoadSectionsForMunicipality(municipality));

        return ResponseEntity.ok(accessibilityResponseMapper.map(accessibility, requestedRoadSectionId));
    }

    private AccessibleRoadSectionModifier addMissingRoadSectionsForMunicipality(Municipality municipality) {
        return (roadsSectionsWithoutAppliedRestrictions, roadSectionsWithAppliedRestrictions) -> {
            List<RoadSection> missingRoadSections = missingRoadSectionProvider.get(
                    municipality.getMunicipalityIdInteger(),
                    roadsSectionsWithoutAppliedRestrictions,
                    false);
            roadsSectionsWithoutAppliedRestrictions.addAll(missingRoadSections);
            roadSectionsWithAppliedRestrictions.addAll(missingRoadSections);
        };
    }

    @Override
    public ResponseEntity<RoadSectionFeatureCollectionJson> getRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer, Boolean accessible, Double latitude,
            Double longitude, EmissionClassJson emissionClass,
            FuelTypeJson fuelType) {
        ensureEnvironmentalZoneParameterConsistency(emissionClass, fuelType);
        Optional<Point> startPoint = mapStartPoint(latitude, longitude);
        boolean startPointPresent = startPoint.isPresent();
        CandidateMatch startPointMatch = startPoint.flatMap(this::matchStartPoint).orElse(null);

        VehicleArguments requestArguments = new VehicleArguments(
                vehicleType,
                vehicleLength, vehicleWidth, vehicleHeight,
                vehicleWeight, vehicleAxleLoad,
                vehicleHasTrailer, emissionClass, fuelType);

        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);
        AccessibilityRequest accessibilityRequest = accessibilityRequestV2Mapper.mapToAccessibilityRequest(clockService.now(), municipality,
                requestArguments);
        Accessibility accessibility = accessibilityService.calculateAccessibility(
                accessibilityRequest,
                addMissingRoadSectionsForMunicipality(municipality));

        return ResponseEntity.ok(roadSectionFeatureCollectionV2Mapper.map(accessibility, startPointPresent, startPointMatch, accessible));
    }

    /**
     * Ensures that the parameters related to environmental zone restrictions are consistent. If one of the parameters is set and the other
     * is not, an exception is thrown.
     *
     * @param emissionClass the emission class information. Can be null, but if it is null, the fuelType must also be null.
     * @param fuelType      the fuel type information. Can be null, but if it is null, the emissionClass must also be null.
     * @throws IncompleteArgumentsException if only one of the parameters is set while the other is not.
     */
    private void ensureEnvironmentalZoneParameterConsistency(EmissionClassJson emissionClass, FuelTypeJson fuelType) {
        if ((emissionClass == null && fuelType != null) || (fuelType == null && emissionClass != null)) {
            throw new IncompleteArgumentsException("If one of the environmental zone parameters is set, the other must be set as well.");
        }

    }

    private Optional<Point> mapStartPoint(Double latitude, Double longitude) {

        pointValidator.validateConsistentValues(latitude, longitude);

        return pointMapper.mapCoordinate(latitude, longitude);
    }

    private Optional<CandidateMatch> matchStartPoint(Point point) {

        Optional<CandidateMatch> candidateMatch = this.pointMatchService.match(point);

        candidateMatch.ifPresent(match -> logStartPointMatch(point, match));

        return candidateMatch;
    }

    private void logStartPointMatch(Point point, CandidateMatch match) {

        log.debug("Found road section id: {} by latitude: {}, longitude {}", match.getMatchedLinkId(), point.getY(), point.getX());
    }
}
