package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.TransportTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.TransportTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeRestrictionJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleTypeRestrictionJsonMapperTest {

    @Mock
    private TransportTypeRestriction transportTypeRestriction;
    @Mock
    private TransportTypeMapper transportTypeMapper;
    @Mock
    private TransportType transportType;
    @Mock
    private VehicleTypeJson vehicleTypeJson;

    @InjectMocks
    private VehicleTypeRestrictionJsonMapper mapper;

    @Test
    void mapToRestrictionJson() {
        when(transportTypeRestriction.getValue()).thenReturn(Set.of(transportType));
        when(transportTypeMapper.map(Set.of(transportType))).thenReturn(List.of(vehicleTypeJson));
        RestrictionJson actual = mapper.mapToRestrictionJson(transportTypeRestriction);

        assertThat(actual)
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
        assertThat(mapper.mapperForType()).isEqualTo(RestrictionType.VEHICLE_TYPE);
    }
}
