package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeReason;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.FuelTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FuelTypeReasonJsonMapperV2 implements AccessibilityReasonJsonMapperV2<FuelTypeReason> {

    private final FuelTypeMapperV2 fuelTypeMapperV2;

    @Override
    public ReasonJson map(FuelTypeReason fuelTypeReason, List<RestrictionJson> restrictions) {
        return new FuelTypeReasonJson()
                .type(TypeEnum.FUEL_TYPE_REASON)
                .values(fuelTypeReason.getValue().stream().map(fuelTypeMapperV2::map).toList())
                .condition(ReasonConditionJson.EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.ENUM)
                .becauseOf(restrictions);
    }

    @Override
    public ReasonType getReasonType() {
        return ReasonType.FUEL_TYPE;
    }
}
