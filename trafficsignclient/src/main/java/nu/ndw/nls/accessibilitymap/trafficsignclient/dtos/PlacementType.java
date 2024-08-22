package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * As copied from nu.ndw.trafficsign.backend.common.controller.api.ApiV4#LOCATION_PLACEMENT_MAP
 */
public enum PlacementType {
    @JsonProperty("L")
    ALONGSIDE,
    @JsonProperty("B")
    ABOVE

}
