package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FuelType {
    ELECTRIC,
    BIODIESEL,
    DIESEL,
    HYDROGEN,
    LPG,
    METHANE,
    PETROL,
    UNKNOWN;
}
