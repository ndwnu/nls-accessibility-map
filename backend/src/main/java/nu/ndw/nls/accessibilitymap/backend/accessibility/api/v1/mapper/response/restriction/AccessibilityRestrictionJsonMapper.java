package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;

public interface AccessibilityRestrictionJsonMapper<T extends AccessibilityRestriction> {

    RestrictionJson mapToRestrictionJson(T accessibilityRestriction);

    RestrictionType mapperForType();
}
