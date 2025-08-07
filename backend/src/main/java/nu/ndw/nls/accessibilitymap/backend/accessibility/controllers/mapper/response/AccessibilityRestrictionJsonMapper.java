package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;

public abstract class AccessibilityRestrictionJsonMapper<T extends AccessibilityRestriction> {

    public abstract RestrictionJson mapToRestrictionJson(T accessibilityRestriction);

    public abstract RestrictionType mapperForType();

}
