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
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.PointMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.RequestMapper;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.backend.services.PointMatchService;
import nu.ndw.nls.accessibilitymap.backend.validators.PointValidator;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.domain.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    private final RequestMapper requestMapper;
    private final AccessibilityResponseMapper accessibilityMapResultMapper;
    private final PointMapper pointMapper;

    private final AccessibilityMapService accessibilityMapService;
    private final PointMatchService pointMatchService;
    private final PointValidator pointValidator;

    @Override
    public ResponseEntity<AccessibilityMapResponseJson> getInaccessibleRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer, Double latitude, Double longitude) {
        checkWeightConstraint(vehicleType, vehicleWeight);
        pointValidator.validateConsistentValues(latitude, longitude);

        Point startPoint = pointMapper.mapCoordinateAllowNulls(latitude, longitude);
        Integer requestedRoadSectionId = null;

        if (Objects.nonNull(startPoint)) {
            CandidateMatch candidateMatch = pointMatchService.match(startPoint).orElseThrow(() ->
                    new PointMatchingRoadSectionNotFoundException("Could not find road section by latitude: " + latitude +
                            " longitude: " + longitude));
            requestedRoadSectionId = candidateMatch.getMatchedLinkId();

            log.debug("Found road section id: {} by latitude: {}, longitude {}", requestedRoadSectionId, latitude,
                    longitude);
        }

        VehicleArguments requestArguments = new VehicleArguments(vehicleType, vehicleLength, vehicleWidth,
                vehicleHeight, vehicleWeight, vehicleAxleLoad, vehicleHasTrailer == Boolean.TRUE);
        VehicleProperties vehicleProperties = requestMapper.mapToVehicleProperties(requestArguments);
        SortedMap<Integer, RoadSection> idToRoadSection = accessibilityMapService
                .determineInaccessibleRoadSections(vehicleProperties, municipalityId);

//      THe requested road section id is probably outside the municipality area
//      Decide what to do in this situation, return nothing? Throw exception?
//        if (    Objects.nonNull(requestedRoadSectionId) &&
//                !idToRoadSection.containsKey(requestedRoadSectionId)) {
//            throw new IllegalStateException("Requested latitude: " + latitude + " longitude: " + longitude +
//                    " matched on road section id: " + requestedRoadSectionId +
//                    ", but this road section id could not be found as accessible road in the municipality id: " +
//                    municipalityId + " area");
//        }

        return ResponseEntity.ok(accessibilityMapResultMapper.map(idToRoadSection, requestedRoadSectionId));
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
