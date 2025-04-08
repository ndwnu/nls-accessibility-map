package nu.ndw.nls.accessibilitymap.backend.controllers;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.AccessibleRoadSectionModifier;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.MissingRoadSectionProvider;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.AccessibilityMapV2ApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.AccessibilityRequestV2Mapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.AccessibilityResponseV2Mapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.PointMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.RoadSectionFeatureCollectionV2Mapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.services.MunicipalityService;
import nu.ndw.nls.accessibilitymap.backend.services.PointMatchService;
import nu.ndw.nls.accessibilitymap.backend.validators.PointValidator;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityMapApiV2DelegateImpl implements AccessibilityMapV2ApiDelegate {

    private final PointValidator pointValidator;

    private final PointMapper pointMapper;

    private final PointMatchService pointMatchService;

    private final AccessibilityResponseV2Mapper accessibilityResponseV2Mapper;

    private final RoadSectionFeatureCollectionV2Mapper roadSectionFeatureCollectionV2Mapper;

    private final MunicipalityService municipalityService;

    private final AccessibilityRequestV2Mapper accessibilityRequestV2Mapper;

    private final AccessibilityService accessibilityService;

    private final MissingRoadSectionProvider accessibilityAddMissingBlockedRoadSections;

    @Override
    public ResponseEntity<AccessibilityMapResponseJson> getInaccessibleRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer, Double latitude, Double longitude) {

        Integer requestedRoadSectionId = mapStartPoint(latitude, longitude)
                .flatMap(this::matchStartPoint)
                .map(CandidateMatch::getMatchedLinkId)
                .orElse(null);

        VehicleArguments requestArguments = new VehicleArguments(
                vehicleType,
                vehicleLength, vehicleWidth, vehicleHeight,
                vehicleWeight, vehicleAxleLoad,
                vehicleHasTrailer);

        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);
        AccessibilityRequest accessibilityRequest = accessibilityRequestV2Mapper.mapToAccessibilityRequest(municipality, requestArguments);

        Accessibility accessibility = accessibilityService.calculateAccessibility(
                accessibilityRequest,
                addMissingRoadSectionsForMunicipality(municipality));

        return ResponseEntity.ok(accessibilityResponseV2Mapper.map(accessibility, requestedRoadSectionId));
    }

    private AccessibleRoadSectionModifier addMissingRoadSectionsForMunicipality(Municipality municipality) {
        return (roadsSectionsWithoutAppliedRestrictions, roadSectionsWithAppliedRestrictions) -> {

            List<RoadSection> missingRoadSections = accessibilityAddMissingBlockedRoadSections.get(
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
            Double longitude) {

        Optional<Point> startPoint = mapStartPoint(latitude, longitude);
        boolean startPointPresent = startPoint.isPresent();
        CandidateMatch startPointMatch = startPoint.flatMap(this::matchStartPoint).orElse(null);

        VehicleArguments requestArguments = new VehicleArguments(
                vehicleType,
                vehicleLength, vehicleWidth, vehicleHeight,
                vehicleWeight, vehicleAxleLoad,
                vehicleHasTrailer);

        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);
        AccessibilityRequest accessibilityRequest = accessibilityRequestV2Mapper.mapToAccessibilityRequest(municipality, requestArguments);
        Accessibility accessibility = accessibilityService.calculateAccessibility(
                accessibilityRequest,
                addMissingRoadSectionsForMunicipality(municipality));
        
        return ResponseEntity.ok(roadSectionFeatureCollectionV2Mapper.map(accessibility, startPointPresent, startPointMatch, accessible));
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
