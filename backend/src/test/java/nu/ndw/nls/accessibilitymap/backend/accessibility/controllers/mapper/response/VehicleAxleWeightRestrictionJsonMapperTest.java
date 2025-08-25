package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleAxleWeightRestrictionJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleAxleWeightRestrictionJsonMapperTest {

    private static final BigDecimal TONS = new BigDecimal("1.00");
    private static final double KILOGRAMS = 1000D;

    @Mock
    private MaximumRestriction maximumRestriction;

    @InjectMocks
    private VehicleAxleWeightRestrictionJsonMapper mapper;

    @Test
    void mapToRestrictionJson() {
        when(maximumRestriction.getValue()).thenReturn(Maximum.builder().value(KILOGRAMS).build());
        RestrictionJson actual = mapper.mapToRestrictionJson(maximumRestriction);

        assertThat(actual)
                .isInstanceOf(VehicleAxleWeightRestrictionJson.class)
                .isEqualTo(getExpected());
    }

    private VehicleAxleWeightRestrictionJson getExpected() {
        return new VehicleAxleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_AXLE_WEIGHT_RESTRICTION)
                .value(TONS)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS);
    }

    @Test
    void mapperForType() {
        assertThat(mapper.mapperForType()).isEqualTo(RestrictionType.VEHICLE_AXLE_LOAD);
    }
}