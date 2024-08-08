package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ZoneCode {
    @JsonProperty("ZB")
    BEGIN,
    @JsonProperty("ZE")
    END,
    @JsonProperty("ZH")
    REPEAT,
    @JsonProperty("ZO")
    UNKNOWN;
}
