package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import jakarta.validation.constraints.NotNull;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.springframework.validation.annotation.Validated;

@Validated
public record AccessibilityNwbRoadSectionUpdate(
        long roadSectionId,

        boolean forwardAccessible,

        boolean backwardAccessible,
        @NotNull
        CarriagewayTypeCode carriagewayTypeCode) {

    public AccessibilityNwbRoadSectionUpdate update(AccessibilityNwbRoadSectionUpdate other) {
        return new AccessibilityNwbRoadSectionUpdate(
                roadSectionId,
                other.forwardAccessible(),
                other.backwardAccessible(),
                other.carriagewayTypeCode());
    }

}
