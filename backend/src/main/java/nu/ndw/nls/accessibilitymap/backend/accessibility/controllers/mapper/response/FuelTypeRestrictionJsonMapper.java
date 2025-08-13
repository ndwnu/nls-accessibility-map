package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.FuelTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionUnitSymbolJson;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeRestrictionJsonMapper extends AccessibilityRestrictionJsonMapper<FuelTypeRestriction> {

    private final FuelTypeMapper fuelTypeMapper;

    public FuelTypeRestrictionJsonMapper(FuelTypeMapper fuelTypeMapper) {
        this.fuelTypeMapper = fuelTypeMapper;
    }

    @Override
    public RestrictionJson mapToRestrictionJson(FuelTypeRestriction fuelTypeRestriction) {
        return new FuelTypeRestrictionJson()
                .type(TypeEnum.FUEL_TYPE_RESTRICTION)
                .values(fuelTypeRestriction.getValue().stream().map(fuelTypeMapper::mapFuelTypeJson).toList())
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM);
    }

    @Override
    public RestrictionType mapperForType() {
        return RestrictionType.FUEL_TYPE;
    }

}
