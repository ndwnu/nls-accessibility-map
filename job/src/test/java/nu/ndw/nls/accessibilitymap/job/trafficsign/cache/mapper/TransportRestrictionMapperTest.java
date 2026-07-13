package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportConditions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.emission.EmissionZoneMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportRestrictionMapperTest {

    private static final String TRAFFIC_REGULATION_ORDER_ID = "traffic-regulation-order-id";

    @Mock
    private TransportConditionsMapper transportConditionsMapper;

    @Mock
    private EmissionZoneMapper emissionZoneMapper;

    @InjectMocks
    private TransportRestrictionMapper transportRestrictionMapper;

    @Mock
    private EmissionZone emissionZone;

    @Mock
    private ConditionPropertiesDtoV5Json restrictionConditionPropertiesDtoV5Json;

    @Mock
    private TransportConditions restrictionTransportConditions;

    @Mock
    private ConditionPropertiesDtoV5Json exemptionConditionPropertiesDtoV5JsonA;

    @Mock
    private ConditionPropertiesDtoV5Json exemptionConditionPropertiesDtoV5JsonB;

    @Mock
    private TransportConditions exemptionTransportConditionsA;

    @Mock
    private TransportConditions exemptionTransportConditionsB;

    @Test
    void map() {
        when(emissionZoneMapper.map(TRAFFIC_REGULATION_ORDER_ID)).thenReturn(emissionZone);
        when(transportConditionsMapper.map(restrictionConditionPropertiesDtoV5Json)).thenReturn(restrictionTransportConditions);
        when(transportConditionsMapper.map(exemptionConditionPropertiesDtoV5JsonA)).thenReturn(exemptionTransportConditionsA);
        when(transportConditionsMapper.map(exemptionConditionPropertiesDtoV5JsonB)).thenReturn(exemptionTransportConditionsB);

        TransportRestrictions result = transportRestrictionMapper.map(
                ConditionsDtoV5Json.builder()
                        .restrictions(restrictionConditionPropertiesDtoV5Json)
                        .exemptions(List.of(exemptionConditionPropertiesDtoV5JsonA, exemptionConditionPropertiesDtoV5JsonB))
                        .build(), TRAFFIC_REGULATION_ORDER_ID);

        assertThat(result.restrictions()).isEqualTo(restrictionTransportConditions);
        assertThat(result.exemptions()).containsExactly(exemptionTransportConditionsA, exemptionTransportConditionsB);
        assertThat(result.emissionZone()).isEqualTo(emissionZone);
    }

    @Test
    void map_nullValues() {
        TransportRestrictions result = transportRestrictionMapper.map(ConditionsDtoV5Json.builder()
                .build(), null);

        assertThat(result.emissionZone()).isNull();
        assertThat(result.exemptions())
                .isEmpty();

        verifyNoInteractions(emissionZoneMapper);
    }

    @Test
    void map_emptyEmissionZoneString() {
        TransportRestrictions result = transportRestrictionMapper.map(ConditionsDtoV5Json.builder()
                .build(), "");

        assertThat(result.emissionZone()).isNull();
        verifyNoInteractions(emissionZoneMapper);
    }
}