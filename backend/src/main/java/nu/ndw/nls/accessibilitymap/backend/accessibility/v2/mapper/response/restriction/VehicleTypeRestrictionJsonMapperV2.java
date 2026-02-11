package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.restriction;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.TransportTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.TransportTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeRestrictionJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleTypeRestrictionJsonMapperV2 implements AccessibilityRestrictionJsonMapperV2<TransportTypeRestriction> {

    private final TransportTypeMapperV2 transportTypeMapperV2;

    @Override
    public RestrictionJson map(TransportTypeRestriction accessibilityRestriction) {
        List<VehicleTypeJson> vehicleTypeJsonList = transportTypeMapperV2.map(accessibilityRestriction.getValue());

        return new VehicleTypeRestrictionJson()
                .type(TypeEnum.VEHICLE_TYPE_RESTRICTION)
                .values(sort(vehicleTypeJsonList))
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM);
    }

    @Override
    public RestrictionType getRestrictionType() {
        return RestrictionType.VEHICLE_TYPE;
    }

    private List<VehicleTypeJson> sort(List<VehicleTypeJson> vehicleTypeJsons) {
        return vehicleTypeJsons.stream()
                .sorted(Comparator.comparing(VehicleTypeJson::getValue))
                .toList();
    }
}
