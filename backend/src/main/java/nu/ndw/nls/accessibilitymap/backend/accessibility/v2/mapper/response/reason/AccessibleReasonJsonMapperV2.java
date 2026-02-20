package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibleReason;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibleReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibleReasonJsonMapperV2 implements AccessibilityReasonJsonMapperV2<AccessibleReason> {

    @Override
    public ReasonJson map(AccessibleReason accessibleReason, List<RestrictionJson> restrictions) {
        return new AccessibleReasonJson()
                .type(TypeEnum.ACCESSIBLE_REASON)
                .value(accessibleReason.getValue())
                .condition(ReasonConditionJson.EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.BOOLEAN)
                .becauseOf(restrictions);
    }

    @Override
    public ReasonType getReasonType() {
        return ReasonType.ACCESSIBLE_REASON;
    }
}
