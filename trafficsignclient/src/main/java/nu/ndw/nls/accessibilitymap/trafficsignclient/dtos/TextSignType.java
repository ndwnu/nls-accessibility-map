package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * As copied from nu.ndw.trafficsign.backend.common.controller.api.ApiV4#TEXT_SIGN_TYPE_MAP
 */
public enum TextSignType {
    @JsonProperty("RICH")
    DIRECTION_ARROWS,
    @JsonProperty("EMIS")
    EMISSION_ZONE,
    @JsonProperty("UIT")
    EXCLUDING,
    @JsonProperty("VRIJ")
    FREE_TEXT,
    @JsonProperty("KEN")
    LICENSE_PLATE,
    @JsonProperty("VOOR")
    PRE_ANNOUNCEMENT,
    @JsonProperty("TIJD")
    TIME_PERIOD

}
