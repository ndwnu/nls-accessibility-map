package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleHeightRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleHeightRestrictionJsonMapper extends AccessibilityRestrictionJsonMapper<MaximumRestriction> {

    private static final int CM_TO_METRES = 2;

    @Override
    public RestrictionJson mapToRestrictionJson(MaximumRestriction maximumRestriction) {
        return new VehicleHeightRestrictionJson()
                .type(TypeEnum.VEHICLE_HEIGHT_RESTRICTION)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.METRE)
                .value(DecimalValueMapper.mapToValue(maximumRestriction.getValue().value(), CM_TO_METRES));
    }

    @Override
    public RestrictionType mapperForType() {
        return RestrictionType.VEHICLE_HEIGHT;
    }
}
