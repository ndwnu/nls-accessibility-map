package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ZoneCodeType {
    START("START"),
    END("END"),
    REPEAT("REPEAT"),
    UNKNOWN("UNKNOWN");

    private final String value;
}
