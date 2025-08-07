package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityReasonService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response.PointMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response.RoadSectionFeatureCollectionMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.validator.PointValidator;
import nu.ndw.nls.accessibilitymap.backend.accessibility.service.PointMatchService;
import nu.ndw.nls.accessibilitymap.backend.exception.IncompleteArgumentsException;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.AccessibilityMapApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    private final PointValidator pointValidator;

    private final PointMapper pointMapper;

    private final GraphHopperService graphHopperService;

    private final PointMatchService pointMatchService;

    private final AccessibilityResponseMapper accessibilityResponseMapper;

    private final RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    private final MunicipalityService municipalityService;

    private final AccessibilityRequestMapper accessibilityRequestMapper;

    private final AccessibilityService accessibilityService;

    private final ClockService clockService;

    private final AccessibilityReasonService accessibilityReasonService;

    @Override
    public ResponseEntity<AccessibilityMapResponseJson> getInaccessibleRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer, Double latitude, Double longitude,
            EmissionClassJson emissionClass,
            FuelTypeJson fuelType) {

        ensureEnvironmentalZoneParameterConsistency(emissionClass, fuelType);

        NetworkGraphHopper networkGraphHopper = graphHopperService.getNetworkGraphHopper();
        Integer requestedRoadSectionId = mapEndpoint(latitude, longitude)
                .flatMap(point -> matchStartPoint(networkGraphHopper, point))
                .map(CandidateMatch::getMatchedLinkId)
                .orElse(null);

        VehicleArguments requestArguments = new VehicleArguments(
                vehicleType,
                vehicleLength, vehicleWidth, vehicleHeight,
                vehicleWeight, vehicleAxleLoad,
                vehicleHasTrailer, emissionClass, fuelType);

        AccessibilityRequest accessibilityRequest = mapToAccessibilityRequest(municipalityId, requestArguments,
                mapEndpoint(latitude, longitude)
                        .orElse(null));

        Accessibility accessibility = accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest);
        List<List<AccessibilityReason>> reasons =
                requestedRoadSectionId == null || accessibility.matchedRoadSectionIsAccessible(requestedRoadSectionId)
                        ? Collections.emptyList()
                        : accessibilityReasonService.getReasons(accessibilityRequest);

        return ResponseEntity.ok(accessibilityResponseMapper.map(accessibility, requestedRoadSectionId, reasons));
    }

    @Override
    public ResponseEntity<RoadSectionFeatureCollectionJson> getRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer, Boolean accessible, Double latitude,
            Double longitude, EmissionClassJson emissionClass,
            FuelTypeJson fuelType) {

        ensureEnvironmentalZoneParameterConsistency(emissionClass, fuelType);

        NetworkGraphHopper networkGraphHopper = graphHopperService.getNetworkGraphHopper();
        Optional<Point> requestedStartPoint = mapEndpoint(latitude, longitude);

        // We are ignoring the bearing because we have only a latitude and longitude so determining the road section is enough to determine
        // a match.
        Long matchedStartPointRoadSectionId = requestedStartPoint
                .flatMap(point -> matchStartPoint(networkGraphHopper, point))
                .map(CandidateMatch::getMatchedLinkId)
                .map(Long::valueOf)
                .orElse(null);

        VehicleArguments requestArguments = new VehicleArguments(
                vehicleType,
                vehicleLength, vehicleWidth, vehicleHeight,
                vehicleWeight, vehicleAxleLoad,
                vehicleHasTrailer, emissionClass, fuelType);

        AccessibilityRequest accessibilityRequest = mapToAccessibilityRequest(municipalityId, requestArguments,
                mapEndpoint(latitude, longitude)
                        .orElse(null));
        Accessibility accessibility = accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest);
        return ResponseEntity.ok(
                roadSectionFeatureCollectionMapper.map(
                        accessibility.combinedAccessibility(),
                        requestedStartPoint.isPresent(),
                        matchedStartPointRoadSectionId,
                        accessible));
    }

    private AccessibilityRequest mapToAccessibilityRequest(String municipalityId, VehicleArguments vehicleArguments, Point endPoint) {
        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);
        return accessibilityRequestMapper.mapToAccessibilityRequest(clockService.now(), municipality, vehicleArguments, endPoint);
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

    private Optional<Point> mapEndpoint(Double latitude, Double longitude) {

        pointValidator.validateConsistentValues(latitude, longitude);

        return pointMapper.mapCoordinate(latitude, longitude);
    }

    private Optional<CandidateMatch> matchStartPoint(NetworkGraphHopper networkGraphHopper, Point point) {

        Optional<CandidateMatch> candidateMatch = this.pointMatchService.match(networkGraphHopper, point);

        candidateMatch.ifPresent(match -> logStartPointMatch(point, match));

        return candidateMatch;
    }

    private void logStartPointMatch(Point point, CandidateMatch match) {

        log.debug("Found road section id: {} by latitude: {}, longitude {}", match.getMatchedLinkId(), point.getY(), point.getX());
    }
}
