package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * As copied from nu.ndw.trafficsign.backend.common.controller.api.ApiV4#ROAD_TYPE_MAP
 */
public enum RoadType {
    @JsonProperty("1")
    A,
    @JsonProperty("2")
    N
}
