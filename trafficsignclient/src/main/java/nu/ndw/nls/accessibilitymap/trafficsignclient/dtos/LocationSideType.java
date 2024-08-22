package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * nu.ndw.trafficsign.backend.common.controller.api.ApiV4#LOCATION_SIDE_MAP
 */
public enum LocationSideType {
    @JsonProperty("L")
    LEFT,
    @JsonProperty("R")
    RIGHT,
    @JsonProperty("N")
    NORTH,
    @JsonProperty("NNO")
    NORTH_NORTH_EAST,
    @JsonProperty("NO")
    NORTH_EAST,
    @JsonProperty("ONO")
    EAST_NORTH_EAST,
    @JsonProperty("O")
    EAST,
    @JsonProperty("OZO")
    EAST_SOUTH_EAST,
    @JsonProperty("ZO")
    SOUTH_EAST,
    @JsonProperty("ZZO")
    SOUTH_SOUTH_EAST,
    @JsonProperty("Z")
    SOUTH,
    @JsonProperty("ZZW")
    SOUTH_SOUTH_WEST,
    @JsonProperty("ZW")
    SOUTH_WEST,
    @JsonProperty("WZW")
    WEST_SOUTH_WEST,
    @JsonProperty("W")
    WEST,
    @JsonProperty("WNW")
    WEST_NORTH_WEST,
    @JsonProperty("NW")
    NORTH_WEST,
    @JsonProperty("NNW")
    NORTH_NORTH_WEST
}
