package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleTypeReasonJsonMapperV2Test {

    private VehicleTypeReasonJsonMapperV2 vehicleTypeReasonJsonMapperV2;

    @Mock
    private TransportTypeMapperV2 transportTypeMapperV2;

    @Mock
    private TransportTypeReason transportTypeReason;

    @Mock
    private TransportType transportType;

    @Mock
    private VehicleTypeJson vehicleTypeJson;

    @Mock
    private RestrictionJson restrictionJson;

    @BeforeEach
    void setUp() {

        vehicleTypeReasonJsonMapperV2 = new VehicleTypeReasonJsonMapperV2(transportTypeMapperV2);
    }

    @Test
    void mapToReasonJson() {

        transportTypeReason = TransportTypeReason.builder()
                .value(Set.of(transportType))
                .build();
        when(transportTypeMapperV2.map(Set.of(transportType))).thenReturn(List.of(vehicleTypeJson));

        ReasonJson reasonJson = vehicleTypeReasonJsonMapperV2.map(transportTypeReason, List.of(restrictionJson));

        assertThat(reasonJson)
                .isInstanceOf(VehicleTypeReasonJson.class)
                .isEqualTo(getExpected());
    }

    private VehicleTypeReasonJson getExpected() {

        return new VehicleTypeReasonJson()
                .type(TypeEnum.VEHICLE_TYPE_REASON)
                .values(List.of(vehicleTypeJson))
                .condition(ReasonConditionJson.EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.ENUM)
                .becauseOf(List.of(restrictionJson));
    }

    @Test
    void mapperForType() {
        assertThat(vehicleTypeReasonJsonMapperV2.getReasonType()).isEqualTo(ReasonType.VEHICLE_TYPE);
    }
}
