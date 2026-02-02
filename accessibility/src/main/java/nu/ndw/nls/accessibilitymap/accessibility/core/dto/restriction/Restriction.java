package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;

public interface Restriction {

    boolean isRestrictive(AccessibilityRequest accessibilityRequest);

    Double networkSnappedLatitude();

    Double networkSnappedLongitude();

    Integer roadSectionId();

    Direction direction();

    boolean isDynamic();
}
