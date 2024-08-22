package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;


/**
 * As copied from nu.ndw.trafficsign.backend.common.controller.api.ApiV4#ROAD_TYPE_MAP
 */
@RequiredArgsConstructor
public enum RoadType {
    A(1),
    N(2);

    private final int value;

    @JsonValue
    public int getValue() {
        return value;
    }
}
