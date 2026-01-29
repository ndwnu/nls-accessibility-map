package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import jakarta.validation.constraints.NotNull;
import org.locationtech.jts.geom.LineString;
import org.springframework.validation.annotation.Validated;

@Validated
public record AccessibilityNwbRoadSection(
        @NotNull long roadSectionId,
        @NotNull long fromNode,
        @NotNull long toNode,
        Integer municipalityId,
        @NotNull LineString geometry,
        @NotNull boolean forwardAccessible,
        @NotNull boolean backwardAccessible) {

}
