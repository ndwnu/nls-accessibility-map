package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.TransportTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeRestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeRestrictionJsonMapper extends AccessibilityRestrictionJsonMapper<TransportTypeRestriction> {

    private final TransportTypeMapper transportTypeMapper;

    public VehicleTypeRestrictionJsonMapper(TransportTypeMapper transportTypeMapper) {
        this.transportTypeMapper = transportTypeMapper;
    }

    @Override
    public RestrictionJson mapToRestrictionJson(TransportTypeRestriction accessibilityRestriction) {
        return new VehicleTypeRestrictionJson()
                .type(TypeEnum.VEHICLE_TYPE_RESTRICTION)
                .values(transportTypeMapper.mapTransportTypeToJson(accessibilityRestriction.getValue()))
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM);
    }

    @Override
    public RestrictionType mapperForType() {
        return RestrictionType.VEHICLE_TYPE;
    }

}
