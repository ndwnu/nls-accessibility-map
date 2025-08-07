package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.RoutePoints;
import org.springframework.stereotype.Component;

@Component
public class RoutePointsMapper {

    public RoutePoints mapToRoutePoints(AccessibilityRequest accessibilityRequest) {
        return RoutePoints.builder()
                .startLocationLatitude(accessibilityRequest.startLocationLatitude())
                .startLocationLongitude(accessibilityRequest.startLocationLongitude())
                .endLocationLatitude(accessibilityRequest.endLocationLatitude())
                .endLocationLongitude(accessibilityRequest.endLocationLongitude())
                .build();
    }
}
