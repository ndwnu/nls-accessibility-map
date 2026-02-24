package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.TransportTypeReason;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.TransportTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeReasonJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleTypeReasonJsonMapperV2 implements AccessibilityReasonJsonMapperV2<TransportTypeReason> {

    private final TransportTypeMapperV2 transportTypeMapperV2;

    @Override
    public ReasonJson map(TransportTypeReason transportTypeReason, List<RestrictionJson> restrictions) {

        List<VehicleTypeJson> vehicleTypeJsonList = transportTypeMapperV2.map(transportTypeReason.getValue());

        return new VehicleTypeReasonJson()
                .type(TypeEnum.VEHICLE_TYPE_REASON)
                .values(sort(vehicleTypeJsonList))
                .condition(ReasonConditionJson.EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.ENUM)
                .becauseOf(restrictions);
    }

    @Override
    public ReasonType getReasonType() {
        return ReasonType.VEHICLE_TYPE;
    }

    private List<VehicleTypeJson> sort(List<VehicleTypeJson> vehicleTypeJsons) {
        return vehicleTypeJsons.stream()
                .sorted(Comparator.comparing(VehicleTypeJson::getValue))
                .toList();
    }
}
