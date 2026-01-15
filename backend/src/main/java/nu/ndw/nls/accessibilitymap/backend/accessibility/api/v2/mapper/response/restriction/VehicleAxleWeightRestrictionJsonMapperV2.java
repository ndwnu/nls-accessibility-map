package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.mapper.DecimalValueMapper;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleAxleWeightRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleAxleWeightRestrictionJsonMapperV2 implements AccessibilityRestrictionJsonMapperV2<MaximumRestriction> {

    private static final int KILOGRAMS_TO_TONS = 3;

    @Override
    public RestrictionJson map(MaximumRestriction maximumRestriction) {
        return new VehicleAxleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_AXLE_WEIGHT_RESTRICTION)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS)
                .value(DecimalValueMapper.mapToValue(maximumRestriction.getValue().value(), KILOGRAMS_TO_TONS));
    }

    @Override
    public RestrictionType getRestrictionType() {
        return RestrictionType.VEHICLE_AXLE_LOAD;
    }
}
