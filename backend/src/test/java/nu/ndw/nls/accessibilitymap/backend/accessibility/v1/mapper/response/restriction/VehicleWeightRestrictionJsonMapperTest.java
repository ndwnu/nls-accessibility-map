package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumReason;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleWeightRestrictionJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleWeightRestrictionJsonMapperTest {

    private static final BigDecimal TONS = new BigDecimal("1.00");
    private static final double KILOGRAMS = 1000D;

    @Mock
    private MaximumReason maximumReason;

    @InjectMocks
    private VehicleWeightRestrictionJsonMapper mapper;

    @Test
    void map() {
        when(maximumReason.getValue()).thenReturn(Maximum.builder().value(KILOGRAMS).build());
        RestrictionJson actual = mapper.map(maximumReason);

        assertThat(actual)
                .isInstanceOf(VehicleWeightRestrictionJson.class)
                .isEqualTo(getExpected());
    }

    private VehicleWeightRestrictionJson getExpected() {
        return new VehicleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_WEIGHT_RESTRICTION)
                .value(TONS)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS);
    }

    @Test
    void mapperForType() {
        assertThat(mapper.mapperForType()).isEqualTo(ReasonType.VEHICLE_WEIGHT);
    }
}
