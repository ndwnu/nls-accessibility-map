package nu.ndw.nls.accessibilitymap.backend.controllers;

import java.util.Objects;
import java.util.SortedMap;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.exceptions.PointMatchingRoadSectionNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.exceptions.VehicleWeightRequiredException;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.AccessibilityMapApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.PointMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.RequestMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.RoadSectionFeatureCollectionMapper;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.backend.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.backend.services.PointMatchService;
import nu.ndw.nls.accessibilitymap.backend.validators.PointValidator;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    private final RequestMapper requestMapper;
    private final AccessibilityResponseMapper accessibilityMapResultMapper;
    private final RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;
    private final PointMapper pointMapper;

    private final AccessibilityMapService accessibilityMapService;
    private final PointMatchService pointMatchService;
    private final PointValidator pointValidator;

    @Override
    public ResponseEntity<AccessibilityMapResponseJson> getInaccessibleRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer, Double latitude, Double longitude) {
        checkWeightConstraint(vehicleType, vehicleWeight);
        CandidateMatch startPointMatch = matchStartPoint(latitude, longitude);
        Integer requestedRoadSectionId = startPointMatch != null ? startPointMatch.getMatchedLinkId() : null;

        VehicleArguments requestArguments = new VehicleArguments(vehicleType, vehicleLength, vehicleWidth,
                vehicleHeight, vehicleWeight, vehicleAxleLoad, vehicleHasTrailer == Boolean.TRUE);
        SortedMap<Integer, RoadSection> idToRoadSection = getAccessibility(municipalityId, requestArguments);

        return ResponseEntity.ok(accessibilityMapResultMapper.map(idToRoadSection, requestedRoadSectionId));
    }

    @Override
    public ResponseEntity<RoadSectionFeatureCollectionJson> getRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer, Boolean accessible, Double latitude,
            Double longitude) {
        checkWeightConstraint(vehicleType, vehicleWeight);
        CandidateMatch startPointMatch = matchStartPoint(latitude, longitude);

        VehicleArguments requestArguments = new VehicleArguments(vehicleType, vehicleLength, vehicleWidth,
                vehicleHeight, vehicleWeight, vehicleAxleLoad, vehicleHasTrailer == Boolean.TRUE);
        SortedMap<Integer, RoadSection> idToRoadSection = getAccessibility(municipalityId, requestArguments);

        return ResponseEntity.ok(roadSectionFeatureCollectionMapper.map(idToRoadSection, startPointMatch, accessible));
    }

    private CandidateMatch matchStartPoint(Double latitude, Double longitude) {
        pointValidator.validateConsistentValues(latitude, longitude);
        Point startPoint = pointMapper.mapCoordinateAllowNulls(latitude, longitude);

        if (Objects.nonNull(startPoint)) {
            CandidateMatch candidateMatch = pointMatchService.match(startPoint).orElseThrow(() ->
                    new PointMatchingRoadSectionNotFoundException("Could not find road section by latitude: " +
                            latitude + " longitude: " + longitude));

            log.debug("Found road section id: {} by latitude: {}, longitude {}", candidateMatch.getMatchedLinkId(),
                    latitude, longitude);

            return candidateMatch;
        }
        return null;
    }

    private SortedMap<Integer, RoadSection> getAccessibility(String municipalityId, VehicleArguments requestArguments) {
        VehicleProperties vehicleProperties = requestMapper.mapToVehicleProperties(requestArguments);
        return accessibilityMapService.determineAccessibilityByRoadSection(vehicleProperties, municipalityId);
    }

    private static void checkWeightConstraint(VehicleTypeJson vehicleType, Float vehicleWeight) {
        if (VehicleTypeJson.COMMERCIAL_VEHICLE == vehicleType && vehicleWeight == null) {
            throw new VehicleWeightRequiredException("When selecting 'commercial_vehicle' as vehicle type "
                    + "vehicle weight is required");
        }
    }

    @Builder
    public record VehicleArguments(VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth,
                                   Float vehicleHeight, Float vehicleWeight, Float vehicleAxleLoad,
                                   boolean vehicleHasTrailer) {
    }
}
