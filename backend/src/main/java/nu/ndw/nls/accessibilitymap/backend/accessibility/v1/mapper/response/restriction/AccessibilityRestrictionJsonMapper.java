package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;

public interface AccessibilityRestrictionJsonMapper<T extends AccessibilityReason> {

    RestrictionJson map(T accessibilityReason);

    ReasonType mapperForType();
}
