package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeReason;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.FuelTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.FuelTypeRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeRestrictionJsonMapper implements AccessibilityRestrictionJsonMapper<FuelTypeReason> {

    private final FuelTypeMapper fuelTypeMapper;

    public FuelTypeRestrictionJsonMapper(FuelTypeMapper fuelTypeMapper) {
        this.fuelTypeMapper = fuelTypeMapper;
    }

    @Override
    public RestrictionJson map(FuelTypeReason fuelTypeReason) {
        return new FuelTypeRestrictionJson()
                .type(TypeEnum.FUEL_TYPE_RESTRICTION)
                .values(fuelTypeReason.getValue().stream().map(fuelTypeMapper::map).toList())
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM);
    }

    @Override
    public ReasonType mapperForType() {
        return ReasonType.FUEL_TYPE;
    }

}
