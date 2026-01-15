package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.FuelTypeMapperV2;
import nu.ndw.nls.accessibilitymap.generated.model.v2.FuelTypeRestrictionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionUnitSymbolJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FuelTypeRestrictionJsonMapperV2 implements AccessibilityRestrictionJsonMapperV2<FuelTypeRestriction> {

    private final FuelTypeMapperV2 fuelTypeMapperV2;

    @Override
    public RestrictionJson map(FuelTypeRestriction fuelTypeRestriction) {
        return new FuelTypeRestrictionJson()
                .type(TypeEnum.FUEL_TYPE_RESTRICTION)
                .values(fuelTypeRestriction.getValue().stream().map(fuelTypeMapperV2::map).toList())
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM);
    }

    @Override
    public RestrictionType getRestrictionType() {
        return RestrictionType.FUEL_TYPE;
    }
}
