package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.converter.LineStringDeserializer;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.converter.LineStringSerializer;
import org.hibernate.validator.constraints.Length;
import org.locationtech.jts.geom.LineString;
import org.springframework.validation.annotation.Validated;

@Validated
public record AccessibilityNwbRoadSection(
        long roadSectionId,

        long fromNode,

        long toNode,

        Integer municipalityId,

        @NotNull
        @JsonDeserialize(using = LineStringDeserializer.class)
        @JsonSerialize(using = LineStringSerializer.class)
        LineString geometry,

        boolean forwardAccessible,

        boolean backwardAccessible,

        @NotNull
        @Length(min = 1, max = 1)
        String functionalRoadClass) {

}
