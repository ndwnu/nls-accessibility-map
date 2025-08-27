package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto;

import java.util.Set;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionZoneTypeJson;

@Builder
public record Excludes(
        Set<String> emissionZoneIds,
        Set<EmissionZoneTypeJson> emissionZoneTypes) {

}
