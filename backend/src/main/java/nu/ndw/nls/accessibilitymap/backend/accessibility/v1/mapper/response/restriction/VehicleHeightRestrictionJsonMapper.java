package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumReason;
import nu.ndw.nls.accessibilitymap.backend.accessibility.mapper.DecimalValueMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleHeightRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleHeightRestrictionJsonMapper implements AccessibilityRestrictionJsonMapper<MaximumReason> {

    private static final int CM_TO_METRES = 2;

    @Override
    public RestrictionJson map(MaximumReason maximumReason) {
        return new VehicleHeightRestrictionJson()
                .type(TypeEnum.VEHICLE_HEIGHT_RESTRICTION)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.METRE)
                .value(DecimalValueMapper.mapToValue(maximumReason.getValue().value(), CM_TO_METRES));
    }

    @Override
    public ReasonType mapperForType() {
        return ReasonType.VEHICLE_HEIGHT;
    }
}
