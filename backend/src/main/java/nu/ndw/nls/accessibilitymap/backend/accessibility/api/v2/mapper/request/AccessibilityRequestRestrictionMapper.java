package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection.RoadSectionRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.NwbRoadSectionSnapService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityContext;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestRoadSectionRestrictionJson;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityRequestRestrictionMapper {

    private final AccessibilityRequestDirectionMapper accessibilityRequestDirectionMapper;

    private final NwbRoadSectionSnapService nwbRoadSectionSnapService;

    public Restriction map(
            AccessibilityContext accessibilityContext,
            AccessibilityRequestRestrictionJson accessibilityRequestRestrictionJson) {

        if (accessibilityRequestRestrictionJson instanceof AccessibilityRequestRoadSectionRestrictionJson roadSectionRestrictionJson) {
            var roadSectionSnappedToGeometryCoordinates = getRoadSectionSnappedToGeometryCoordinates(
                    accessibilityContext,
                    roadSectionRestrictionJson);

            return RoadSectionRestriction.builder()
                    .id(roadSectionRestrictionJson.getId())
                    .direction(accessibilityRequestDirectionMapper.map(roadSectionRestrictionJson.getDirection()))
                    .fraction(roadSectionRestrictionJson.getFraction().doubleValue())
                    .networkSnappedLatitude(roadSectionSnappedToGeometryCoordinates.coordinate().getY())
                    .networkSnappedLongitude(roadSectionSnappedToGeometryCoordinates.coordinate().getX())
                    .build();
        }
        throw new ApiException(
                UUID.fromString("c1f70586-ced2-43b8-b0fc-038dccad31ee"),
                HttpStatus.BAD_REQUEST,
                "Invalid restriction type",
                "Restriction type '%s' is not a valid restriction type. Please check the api specification for valid options."
                        .formatted(accessibilityRequestRestrictionJson.getClass().getSimpleName()));
    }

    private CoordinateAndBearing getRoadSectionSnappedToGeometryCoordinates(
            AccessibilityContext accessibilityContext,
            AccessibilityRequestRoadSectionRestrictionJson roadSectionRestrictionJson) {

        return accessibilityContext.findAllAccessibilityNwbRoadSectionById(roadSectionRestrictionJson.getId())
                .map(accessibilityNwbRoadSection -> nwbRoadSectionSnapService.snapToLine(
                        accessibilityNwbRoadSection.geometry(),
                        roadSectionRestrictionJson.getFraction().doubleValue()))
                .orElseThrow(() -> new ApiException(
                        UUID.fromString("355aba7d-4106-4aec-b0fc-94620647b37d"),
                        HttpStatus.BAD_REQUEST,
                        "Invalid road section restriction",
                        "Road section with id '%s' available in NWB version '%s'. Please try a different road section."
                                .formatted(roadSectionRestrictionJson.getId(), accessibilityContext.nwbVersionId())));
    }
}
