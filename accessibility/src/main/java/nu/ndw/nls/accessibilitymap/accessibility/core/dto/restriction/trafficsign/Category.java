package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    CHARGING("Charging"),
    LOADING("Loading"),
    PERMIT("Permit"),
    LOCAL_TRAFFIC("Local traffic"),
    DISABLED_TRANSPORT("Disabled transport"),
    DANGEROUS_SUPPLIES("Dangerous supplies");

    private final String value;
}
