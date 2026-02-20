package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection.RoadSectionRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.TransportTypeReason;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction.AccessibilityRestrictionJsonMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.TrafficSignTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonsJsonMapperTest {

    private AccessibilityReasonsJsonMapper accessibilityReasonsJsonMapper;

    @Mock
    private AccessibilityReason accessibilityReason;

    @Mock
    private TrafficSign trafficSign;

    @Mock
    private FuelTypeReason fuelTypeReason;

    @Mock
    private TransportTypeReason transportTypeReason;

    @Mock
    private AccessibilityRestrictionJsonMapper<AccessibilityReason> accessibilityRestrictionJsonMapper;

    @Mock
    private ReasonType reasonType;

    @Mock
    private RestrictionJson restrictionJson;


    @BeforeEach
    void setup() {

        when(accessibilityRestrictionJsonMapper.mapperForType()).thenReturn(reasonType);

        accessibilityReasonsJsonMapper = new AccessibilityReasonsJsonMapper(List.of(accessibilityRestrictionJsonMapper));
    }

    @ParameterizedTest
    @EnumSource(TrafficSignType.class)
    void mapToReasonJson(TrafficSignType trafficSignType) {

        when(accessibilityReason.getRestrictions()).thenReturn(Set.of(trafficSign));
        when(accessibilityReason.getReasonType()).thenReturn(reasonType);
        when(trafficSign.externalId()).thenReturn("4feeabc3-19ad-459f-9b8e-37116c22c512");
        when(trafficSign.trafficSignType()).thenReturn(trafficSignType);

        when(accessibilityRestrictionJsonMapper.map(accessibilityReason)).thenReturn(restrictionJson);

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapper.mapToReasonJson(
                List.of(new AccessibilityReasonGroup(List.of(accessibilityReason))));

        ReasonJson expected = new ReasonJson()
                .trafficSignId(java.util.UUID.fromString("4feeabc3-19ad-459f-9b8e-37116c22c512"))
                .trafficSignType(TrafficSignTypeJson.fromValue(trafficSignType.getRvvCode()))
                .restrictions(List.of(restrictionJson));
        List<List<ReasonJson>> expectedList = List.of(List.of(expected));

        assertThat(actual).isEqualTo(expectedList);
    }

    @Test
    void mapToReasonJson_noRestriction( ) {

        when(accessibilityReason.getRestrictions()).thenReturn(Set.of());

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapper.mapToReasonJson(
                List.of(new AccessibilityReasonGroup(List.of(accessibilityReason))));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst()).isEmpty();
    }

    @Test
    void mapToReasonJson_unsupportedRestriction() {

        when(accessibilityReason.getRestrictions()).thenReturn(Set.of(mock(RoadSectionRestriction.class)));

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapper.mapToReasonJson(
                List.of(new AccessibilityReasonGroup(List.of(accessibilityReason))));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(TrafficSignType.class)
    void mapToReasonJson_nonExistingMapper(TrafficSignType trafficSignType) {
        when(accessibilityReason.getRestrictions()).thenReturn(Set.of(trafficSign));
        when(accessibilityReason.getReasonType()).thenReturn(mock(ReasonType.class));
        when(trafficSign.externalId()).thenReturn("4feeabc3-19ad-459f-9b8e-37116c22c512");
        when(trafficSign.trafficSignType()).thenReturn(trafficSignType);

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapper.mapToReasonJson(
                List.of(new AccessibilityReasonGroup(List.of(accessibilityReason))));

        ReasonJson expected = new ReasonJson()
                .trafficSignId(java.util.UUID.fromString("4feeabc3-19ad-459f-9b8e-37116c22c512"))
                .trafficSignType(TrafficSignTypeJson.fromValue(trafficSignType.getRvvCode()))
                .restrictions(List.of());
        List<List<ReasonJson>> expectedList = List.of(List.of(expected));

        assertThat(actual).isEqualTo(expectedList);
    }
}
