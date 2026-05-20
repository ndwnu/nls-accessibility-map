package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.hibernate.validator.constraints.Length;
import org.locationtech.jts.geom.LineString;
import org.springframework.validation.annotation.Validated;

@Validated
public record AccessibilityNwbRoadSection(
        long roadSectionId,

        long fromNode,

        long toNode,

        Integer municipalityId,

        // This field is not stored on disk cache to save memory usage and load times.
        // The geometry is only provided while reading from the database and passed on to create a graphhopper network.
        // Per request this geometry is not used but retrieved from the network see NetworkData.findGeometryInNetwork
        @Nullable
        @JsonIgnore
        LineString geometry,

        boolean forwardAccessible,

        boolean backwardAccessible,

        @NotNull
        CarriagewayTypeCode carriagewayTypeCode,

        @NotNull
        @Length(min = 1, max = 1)
        String functionalRoadClass) {

}
