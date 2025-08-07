package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import lombok.Builder;

@Builder
public record RoutePoints(Double startLocationLatitude,
                          Double startLocationLongitude,
                          Double endLocationLatitude,
                          Double endLocationLongitude) {

}
