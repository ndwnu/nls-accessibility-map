package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ZoneCodeType {
    @JsonProperty("ZB")
    BEGIN("ZB"),
    @JsonProperty("ZE")
    END("ZE"),
    @JsonProperty("ZH")
    REPEAT("ZH"),
    @JsonProperty("ZO")
    UNKNOWN("ZO");

    private final String value;
}
