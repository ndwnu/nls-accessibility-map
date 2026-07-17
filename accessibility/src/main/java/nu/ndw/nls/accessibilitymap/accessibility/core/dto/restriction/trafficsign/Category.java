package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    LOCAL_TRAFFIC("Local traffic");

    private final String value;
}
