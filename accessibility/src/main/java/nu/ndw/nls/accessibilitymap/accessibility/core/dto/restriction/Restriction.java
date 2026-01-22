package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;

public interface Restriction {

    boolean isRestrictive(AccessibilityRequest accessibilityRequest);
}
