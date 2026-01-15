package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionJson;

public interface AccessibilityRestrictionJsonMapperV2<T extends AccessibilityRestriction> {

    RestrictionJson map(T accessibilityRestriction);

    RestrictionType getRestrictionType();
}
