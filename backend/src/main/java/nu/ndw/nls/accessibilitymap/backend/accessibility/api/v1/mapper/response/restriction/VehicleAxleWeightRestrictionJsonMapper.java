package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.mapper.DecimalValueMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleAxleWeightRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleAxleWeightRestrictionJsonMapper implements AccessibilityRestrictionJsonMapper<MaximumRestriction> {

    private static final int KILOGRAMS_TO_TONS = 3;

    @Override
    public RestrictionJson mapToRestrictionJson(MaximumRestriction maximumRestriction) {
        return new VehicleAxleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_AXLE_WEIGHT_RESTRICTION)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS)
                .value(DecimalValueMapper.mapToValue(maximumRestriction.getValue().value(), KILOGRAMS_TO_TONS));
    }

    @Override
    public RestrictionType mapperForType() {
        return RestrictionType.VEHICLE_AXLE_LOAD;
    }
}
