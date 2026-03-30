package nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto;

import jakarta.validation.constraints.NotNull;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.springframework.validation.annotation.Validated;

@Validated
public record ChangedNwbRoadSection(
        long roadSectionId,

        boolean forwardAccessible,

        boolean backwardAccessible,
        @NotNull
        CarriagewayTypeCode carriagewayTypeCode) {

}
