package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumReason;
import nu.ndw.nls.accessibilitymap.backend.accessibility.mapper.DecimalValueMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleWeightRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleWeightRestrictionJsonMapper implements AccessibilityRestrictionJsonMapper<MaximumReason> {

    private static final int KILOGRAMS_TO_TONS = 3;

    @Override
    public RestrictionJson map(MaximumReason maximumReason) {
        return new VehicleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_WEIGHT_RESTRICTION)
                .value(DecimalValueMapper.mapToValue(maximumReason.getValue().value(), KILOGRAMS_TO_TONS))
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS);
    }

    @Override
    public ReasonType mapperForType() {
        return ReasonType.VEHICLE_WEIGHT;
    }
}
