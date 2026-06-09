package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection.RoadSectionRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestRoadSectionRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DirectionJson;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.locationtech.jts.geom.LineString;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityRequestRestrictionMapper {

    private static final String ROAD_SECTION_NOT_FOUND_MESSAGE = "Road section with id '%s' not found in NWB version '%s'.";

    private final AccessibilityRequestDirectionMapper accessibilityRequestDirectionMapper;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    public Restriction map(
            NetworkData networkData,
            AccessibilityRequestRestrictionJson accessibilityRequestRestrictionJson
    ) {

        if (accessibilityRequestRestrictionJson instanceof AccessibilityRequestRoadSectionRestrictionJson roadSectionRestrictionJson) {
            var roadSectionSnappedToGeometryCoordinates = getRoadSectionSnappedToGeometryCoordinates(
                    networkData,
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
            NetworkData networkData,
            AccessibilityRequestRoadSectionRestrictionJson roadSectionRestrictionJson
    ) {

        LineString geometry = networkData.findGeometryInNetwork(roadSectionRestrictionJson.getId())
                .orElseThrow(() -> createApiException(
                        networkData,
                                roadSectionRestrictionJson
                        )
                );
            return fractionAndDistanceCalculator.getCoordinateAndBearing(
                    roadSectionRestrictionJson.getDirection() == DirectionJson.FORWARD ? geometry : geometry.reverse(),
                    roadSectionRestrictionJson.getFraction().doubleValue());

    }

    private ApiException createApiException(NetworkData networkData,
            AccessibilityRequestRoadSectionRestrictionJson roadSectionRestrictionJson
    ) {
        return new ApiException(
                UUID.fromString("355aba7d-4106-4aec-b0fc-94620647b37d"),
                HttpStatus.BAD_REQUEST,
                "Invalid road section restriction",
                AccessibilityRequestRestrictionMapper.ROAD_SECTION_NOT_FOUND_MESSAGE
                        .formatted(roadSectionRestrictionJson.getId(), networkData.getNwbVersion()));
    }
}
