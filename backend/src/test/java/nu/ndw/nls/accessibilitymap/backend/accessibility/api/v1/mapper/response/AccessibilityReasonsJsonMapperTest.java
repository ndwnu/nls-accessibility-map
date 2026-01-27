package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response;

import static nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType.FUEL_TYPE;
import static nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType.VEHICLE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.restriction.FuelTypeRestrictionJsonMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.FuelTypeRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.TrafficSignTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonsJsonMapperTest {

    private static final String UUID = "4feeabc3-19ad-459f-9b8e-37116c22c512";

    private AccessibilityReasonsJsonMapper accessibilityReasonsJsonMapper;

    @Mock
    private AccessibilityReason accessibilityReason;

    @Mock
    private FuelTypeRestriction fuelTypeRestriction;

    @Mock
    private TransportTypeRestriction transportTypeRestriction;

    @Mock
    private FuelTypeRestrictionJsonMapper fuelTypeRestrictionJsonMapper;

    @BeforeEach
    void setup() {

        when(fuelTypeRestrictionJsonMapper.mapperForType()).thenReturn(FUEL_TYPE);

        accessibilityReasonsJsonMapper = new AccessibilityReasonsJsonMapper(List.of(fuelTypeRestrictionJsonMapper));
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

        when(fuelTypeRestrictionJsonMapper.mapToRestrictionJson(fuelTypeRestriction)).thenReturn(fuelTypeRestrictionJson);

        List<List<AccessibilityReason>> accessibilityReasons = List.of(List.of(accessibilityReason));

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapper.mapToReasonJson(accessibilityReasons);

        ReasonJson expected = new ReasonJson()
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

        List<List<AccessibilityReason>> accessibilityReasons = List.of(List.of(accessibilityReason));

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapper.mapToReasonJson(accessibilityReasons);

        ReasonJson expected = new ReasonJson()
                .trafficSignId(java.util.UUID.fromString(UUID))
                .trafficSignType(TrafficSignTypeJson.fromValue(trafficSignType.getRvvCode()))
                .restrictions(List.of());
        List<List<ReasonJson>> expectedList = List.of(List.of(expected));

        assertThat(actual).isEqualTo(expectedList);
    }
}
