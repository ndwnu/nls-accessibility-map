package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;

public interface AccessibilityRestrictionJsonMapperV2<T extends Restriction> {

    RestrictionJson map(T Restriction);

    Class<? extends Restriction> getRestrictionType();
}
