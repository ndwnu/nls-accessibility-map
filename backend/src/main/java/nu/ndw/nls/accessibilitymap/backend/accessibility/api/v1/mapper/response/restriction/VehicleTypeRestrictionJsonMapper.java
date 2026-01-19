package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.restriction;

import java.util.Comparator;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.TransportTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeRestrictionJsonMapper implements AccessibilityRestrictionJsonMapper<TransportTypeRestriction> {

    private final TransportTypeMapper transportTypeMapper;

    public VehicleTypeRestrictionJsonMapper(TransportTypeMapper transportTypeMapper) {
        this.transportTypeMapper = transportTypeMapper;
    }

    @Override
    public RestrictionJson mapToRestrictionJson(TransportTypeRestriction accessibilityRestriction) {
        List<VehicleTypeJson> vehicleTypeJsonList = transportTypeMapper.map(accessibilityRestriction.getValue());

        return new VehicleTypeRestrictionJson()
                .type(TypeEnum.VEHICLE_TYPE_RESTRICTION)
                .values(sort(vehicleTypeJsonList))
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM);
    }

    @Override
    public RestrictionType mapperForType() {
        return RestrictionType.VEHICLE_TYPE;
    }

    private List<VehicleTypeJson> sort(List<VehicleTypeJson> vehicleTypeJsons) {
        return vehicleTypeJsons.stream()
                .sorted(Comparator.comparing(VehicleTypeJson::getValue))
                .toList();
    }

}
