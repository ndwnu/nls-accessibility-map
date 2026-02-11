package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;

public interface AccessibilityRestrictionJsonMapper<T extends AccessibilityRestriction> {

    RestrictionJson mapToRestrictionJson(T accessibilityRestriction);

    RestrictionType mapperForType();
}
