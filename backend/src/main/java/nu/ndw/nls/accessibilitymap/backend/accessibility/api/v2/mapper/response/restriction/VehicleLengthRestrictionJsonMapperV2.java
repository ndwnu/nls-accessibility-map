package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.mapper.DecimalValueMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleLengthRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleLengthRestrictionJsonMapperV2 implements AccessibilityRestrictionJsonMapperV2<MaximumRestriction> {

    private static final int CM_TO_METRES = 2;

    @Override
    public RestrictionJson map(MaximumRestriction maximumRestriction) {
        return new VehicleLengthRestrictionJson()
                .type(TypeEnum.VEHICLE_LENGTH_RESTRICTION)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.METRE)
                .value(DecimalValueMapper.mapToValue(maximumRestriction.getValue().value(), CM_TO_METRES));
    }

    @Override
    public RestrictionType getRestrictionType() {
        return RestrictionType.VEHICLE_LENGTH;
    }
}
