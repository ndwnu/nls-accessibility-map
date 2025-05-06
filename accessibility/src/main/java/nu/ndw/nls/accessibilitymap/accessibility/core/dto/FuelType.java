package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FuelType {
    COMPRESSED_NATURAL_GAS,
    DIESEL,
    ETHANOL,
    ELECTRIC,
    HYDROGEN,
    LIQUEFIED_PETROLEUM_GAS,
    LIQUEFIED_NATURAL_GAS,
    PETROL,
    UNKNOWN
}
