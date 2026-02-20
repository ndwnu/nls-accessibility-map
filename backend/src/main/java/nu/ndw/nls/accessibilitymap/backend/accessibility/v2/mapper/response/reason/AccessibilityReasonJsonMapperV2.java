package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;

public interface AccessibilityReasonJsonMapperV2<T extends AccessibilityReason> {

    ReasonJson map(T AccessibilityReason, List<RestrictionJson> restrictions);

    ReasonType getReasonType();

}
