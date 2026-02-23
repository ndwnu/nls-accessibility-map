package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;

public interface RestrictionMapper {

    @SuppressWarnings("java:S1452")
    List<AccessibilityReason<?>> mapRestrictions(Restrictions restrictions);
}
