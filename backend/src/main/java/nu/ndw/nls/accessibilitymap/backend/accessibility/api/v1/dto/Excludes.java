package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.dto;

import java.util.Set;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionZoneTypeJson;

@Builder
public record Excludes(
        Set<String> emissionZoneIds,
        Set<EmissionZoneTypeJson> emissionZoneTypes) {

}
