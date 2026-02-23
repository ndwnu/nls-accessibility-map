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
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleAxleWeightReasonJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleAxleWeightReasonJsonMapperV2 implements AccessibilityReasonJsonMapperV2<MaximumReason> {

    private static final int KILOGRAMS_TO_TONS = 3;

    @Override
    public ReasonJson map(MaximumReason maximumReason, List<RestrictionJson> restrictions) {
        return new VehicleAxleWeightReasonJson()
                .type(TypeEnum.VEHICLE_AXLE_WEIGHT_REASON)
                .condition(ReasonConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.TONS)
                .value(DecimalValueMapper.mapToValue(maximumReason.getValue().value(), KILOGRAMS_TO_TONS))
                .becauseOf(restrictions);
    }

    @Override
    public ReasonType getReasonType() {
        return ReasonType.VEHICLE_AXLE_LOAD;
    }
}
