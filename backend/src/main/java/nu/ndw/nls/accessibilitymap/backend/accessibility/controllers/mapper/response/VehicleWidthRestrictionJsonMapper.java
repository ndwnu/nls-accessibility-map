package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleWidthRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleWidthRestrictionJsonMapper extends AccessibilityRestrictionJsonMapper<MaximumRestriction> {

    private static final int CM_TO_METRES = 2;

    @Override
    public RestrictionJson mapToRestrictionJson(MaximumRestriction accessibilityRestriction) {
        return new VehicleWidthRestrictionJson()
                .type(TypeEnum.VEHICLE_WIDTH_RESTRICTION)
                .value(mapToValue(accessibilityRestriction))
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.METRE);
    }

    private static BigDecimal mapToValue(MaximumRestriction accessibilityRestriction) {
        return BigDecimal.valueOf(accessibilityRestriction
                        .getValue().value())
                .movePointLeft(CM_TO_METRES)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public RestrictionType mapperForType() {
        return RestrictionType.VEHICLE_WIDTH;
    }
}
