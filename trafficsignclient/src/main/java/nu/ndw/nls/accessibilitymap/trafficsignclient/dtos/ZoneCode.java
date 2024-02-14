package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum ZoneCode {
    @JsonProperty("ZB")
    ZONE_BEGINS,
    @JsonProperty("ZE")
    ZONE_ENDS;
}
