package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * nu.ndw.trafficsign.backend.common.controller.api.ApiV4#DIRECTION_MAP
 */
public enum DirectionType {
    @JsonProperty("B")
    BOTH,
    @JsonProperty("H")
    FORTH,
    @JsonProperty("T")
    BACK
}
