package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response;

import static nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType.FUEL_TYPE;
import static nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType.VEHICLE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.TransportTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction.FuelTypeRestrictionJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonsJsonMapperV2Test {

    private static final String UUID = "4feeabc3-19ad-459f-9b8e-37116c22c512";

    private AccessibilityReasonsJsonMapperV2 accessibilityReasonsJsonMapperV2;

    @Mock
    private AccessibilityReason accessibilityReason;

    @Mock
    private FuelTypeRestriction fuelTypeRestriction;

    @Mock
    private TransportTypeRestriction transportTypeRestriction;

    @Mock
    private FuelTypeRestrictionJsonMapperV2 fuelTypeRestrictionJsonMapperV2;

    @BeforeEach
    void setup() {

        when(fuelTypeRestrictionJsonMapperV2.getRestrictionType()).thenReturn(FUEL_TYPE);

        accessibilityReasonsJsonMapperV2 = new AccessibilityReasonsJsonMapperV2(List.of(fuelTypeRestrictionJsonMapperV2));
    }

    @ParameterizedTest
    @EnumSource(TrafficSignType.class)
    void mapToReasonJson(TrafficSignType trafficSignType) {

        when(accessibilityReason.trafficSignExternalId()).thenReturn(UUID);
        when(accessibilityReason.trafficSignType()).thenReturn(trafficSignType);
        when(accessibilityReason.restrictions()).thenReturn(List.of(fuelTypeRestriction));

        when(fuelTypeRestriction.getTypeOfRestriction()).thenReturn(FUEL_TYPE);

        FuelTypeRestrictionJson fuelTypeRestrictionJson = new FuelTypeRestrictionJson()
                .type(TypeEnum.FUEL_TYPE_RESTRICTION)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM)
                .condition(RestrictionConditionJson.EQUALS)
                .values(List.of(FuelTypeJson.DIESEL));

        when(fuelTypeRestrictionJsonMapperV2.map(fuelTypeRestriction)).thenReturn(fuelTypeRestrictionJson);

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapperV2.map(List.of(List.of(accessibilityReason)));

        ReasonJson expected = new TrafficSignReasonJson()
                .trafficSignId(java.util.UUID.fromString(UUID))
                .trafficSignType(TrafficSignTypeJson.fromValue(trafficSignType.getRvvCode()))
                .restrictions(List.of(fuelTypeRestrictionJson));
        List<List<ReasonJson>> expectedList = List.of(List.of(expected));

        assertThat(actual).isEqualTo(expectedList);
    }

    @ParameterizedTest
    @EnumSource(TrafficSignType.class)
    void mapToReasonJson_nonExistingMapper(TrafficSignType trafficSignType) {

        when(accessibilityReason.trafficSignExternalId()).thenReturn(UUID);
        when(accessibilityReason.trafficSignType()).thenReturn(trafficSignType);
        when(accessibilityReason.restrictions()).thenReturn(List.of(transportTypeRestriction));
        when(transportTypeRestriction.getTypeOfRestriction()).thenReturn(VEHICLE_TYPE);

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapperV2.map(List.of(List.of(accessibilityReason)));

        ReasonJson expected = new TrafficSignReasonJson()
                .trafficSignId(java.util.UUID.fromString(UUID))
                .trafficSignType(TrafficSignTypeJson.fromValue(trafficSignType.getRvvCode()))
                .restrictions(List.of());
        List<List<ReasonJson>> expectedList = List.of(List.of(expected));

        assertThat(actual).isEqualTo(expectedList);
    }
}
