package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.mapper.DecimalValueMapper;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.VehicleWeightRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleWeightRestrictionJsonMapper implements AccessibilityRestrictionJsonMapper<MaximumRestriction> {

    private static final int KILOGRAMS_TO_TONS = 3;

    @Override
    public RestrictionJson mapToRestrictionJson(MaximumRestriction maximumRestriction) {
        return new VehicleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_WEIGHT_RESTRICTION)
                .value(DecimalValueMapper.mapToValue(maximumRestriction.getValue().value(), KILOGRAMS_TO_TONS))
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS);
    }

    @Override
    public RestrictionType mapperForType() {
        return RestrictionType.VEHICLE_WEIGHT;
    }
}
