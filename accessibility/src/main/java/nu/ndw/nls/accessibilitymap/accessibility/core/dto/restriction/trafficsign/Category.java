package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Category {
    CHARGING("charging"),
    LOADING("loading"),
    PERMIT("permit"),
    LOCAL_TRAFFIC("localTraffic"),
    DISABLED_TRANSPORT("disabledTransport"),
    DANGEROUS_SUPPLIES("dangerousSupplies");

    private final String category;
}
