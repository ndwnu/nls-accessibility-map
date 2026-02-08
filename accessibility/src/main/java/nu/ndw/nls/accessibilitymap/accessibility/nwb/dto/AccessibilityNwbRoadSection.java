package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.converter.LineStringDeserializer;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.converter.LineStringSerializer;
import org.locationtech.jts.geom.LineString;
import org.springframework.validation.annotation.Validated;

@Validated
public record AccessibilityNwbRoadSection(
        @NotNull
        long roadSectionId,

        @NotNull
        long fromNode,

        @NotNull
        long toNode,

        Integer municipalityId,

        @NotNull
        @JsonDeserialize(using = LineStringDeserializer.class)
        @JsonSerialize(using = LineStringSerializer.class)
        LineString geometry,

        @NotNull
        boolean forwardAccessible,

        @NotNull
        boolean backwardAccessible) {

}
