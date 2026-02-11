package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.mapper.DecimalValueMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleLengthRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleLengthRestrictionJsonMapper implements AccessibilityRestrictionJsonMapper<MaximumRestriction> {

    private static final int CM_TO_METRES = 2;

    @Override
    public RestrictionJson mapToRestrictionJson(MaximumRestriction maximumRestriction) {
        return new VehicleLengthRestrictionJson()
                .type(TypeEnum.VEHICLE_LENGTH_RESTRICTION)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.METRE)
                .value(DecimalValueMapper.mapToValue(maximumRestriction.getValue().value(), CM_TO_METRES));
    }

    @Override
    public RestrictionType mapperForType() {
        return RestrictionType.VEHICLE_LENGTH;
    }
}
