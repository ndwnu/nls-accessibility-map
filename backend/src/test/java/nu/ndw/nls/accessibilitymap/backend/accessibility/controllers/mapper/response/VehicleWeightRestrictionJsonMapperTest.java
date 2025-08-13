package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleWeightRestrictionJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleWeightRestrictionJsonMapperTest {

    private static final BigDecimal TONS = new BigDecimal(1);
    private static final double KILOGRAMS = 1000D;
    VehicleWeightRestrictionJsonMapper mapper;

    @Mock
    private MaximumRestriction maximumRestriction;

    @Test
    void mapToRestrictionJson() {
        when(maximumRestriction.getValue().value()).thenReturn(KILOGRAMS);
        RestrictionJson actual = mapper.mapToRestrictionJson(maximumRestriction);

        assertThat(actual).isInstanceOf(VehicleWeightRestrictionJson.class);
        assertThat(actual).isEqualTo(getExpected());
    }

    private VehicleWeightRestrictionJson getExpected() {
        return new VehicleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_WEIGHT_RESTRICTION)
                .value(TONS)
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS);
    }

    @Test
    void mapperForType() {
        assertThat(mapper.mapperForType()).isEqualTo(RestrictionType.VEHICLE_WEIGHT);
    }
}
