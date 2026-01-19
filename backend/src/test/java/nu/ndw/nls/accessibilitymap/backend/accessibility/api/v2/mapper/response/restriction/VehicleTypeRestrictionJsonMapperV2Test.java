package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.TransportTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeRestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleTypeRestrictionJsonMapperV2Test {

    private VehicleTypeRestrictionJsonMapperV2 vehicleTypeRestrictionJsonMapperV2;

    @Mock
    private TransportTypeMapperV2 transportTypeMapperV2;

    @Mock
    private TransportTypeRestriction transportTypeRestriction;

    @Mock
    private TransportType transportType;

    @Mock
    private VehicleTypeJson vehicleTypeJson;

    @BeforeEach
    void setUp() {

        vehicleTypeRestrictionJsonMapperV2 = new VehicleTypeRestrictionJsonMapperV2(transportTypeMapperV2);
    }

    @Test
    void mapToRestrictionJson() {

        transportTypeRestriction = TransportTypeRestriction.builder()
                .value(Set.of(transportType))
                .build();
        when(transportTypeMapperV2.map(Set.of(transportType))).thenReturn(List.of(vehicleTypeJson));

        RestrictionJson restrictionJson = vehicleTypeRestrictionJsonMapperV2.map(transportTypeRestriction);

        assertThat(restrictionJson)
                .isInstanceOf(VehicleTypeRestrictionJson.class)
                .isEqualTo(getExpected());
    }

    private VehicleTypeRestrictionJson getExpected() {

        return new VehicleTypeRestrictionJson()
                .type(TypeEnum.VEHICLE_TYPE_RESTRICTION)
                .values(List.of(vehicleTypeJson))
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM);
    }

    @Test
    void mapperForType() {
        assertThat(vehicleTypeRestrictionJsonMapperV2.getRestrictionType()).isEqualTo(RestrictionType.VEHICLE_TYPE);
    }
}
