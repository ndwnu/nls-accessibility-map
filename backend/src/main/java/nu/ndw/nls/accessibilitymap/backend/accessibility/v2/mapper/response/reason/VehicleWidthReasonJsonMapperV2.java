package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumReason;
import nu.ndw.nls.accessibilitymap.backend.accessibility.mapper.DecimalValueMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleWidthReasonJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleWidthReasonJsonMapperV2 implements AccessibilityReasonJsonMapperV2<MaximumReason> {

    private static final int CM_TO_METRES = 2;

    @Override
    public ReasonJson map(MaximumReason maximumReason, List<RestrictionJson> restrictions) {
        return new VehicleWidthReasonJson()
                .type(TypeEnum.VEHICLE_WIDTH_REASON)
                .value(DecimalValueMapper.mapToValue(maximumReason.getValue().value(), CM_TO_METRES))
                .condition(ReasonConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.METRE)
                .becauseOf(restrictions);
    }

    @Override
    public ReasonType getReasonType() {
        return ReasonType.VEHICLE_WIDTH;
    }
}
