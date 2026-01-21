package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.restriction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleHeightRestrictionJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleHeightRestrictionJsonMapperTest {

    private static final BigDecimal METERS = new BigDecimal("1.00");
    private static final double CENTIMETERS = 100D;

    @Mock
    private MaximumRestriction maximumRestriction;

    @InjectMocks
    private VehicleHeightRestrictionJsonMapper mapper;

    @Test
    void mapToRestrictionJson() {
        when(maximumRestriction.getValue()).thenReturn(Maximum.builder().value(CENTIMETERS).build());
        RestrictionJson actual = mapper.mapToRestrictionJson(maximumRestriction);

        assertThat(actual)
                .isInstanceOf(VehicleHeightRestrictionJson.class)
                .isEqualTo(getExpected());
    }

    private VehicleHeightRestrictionJson getExpected() {
        return new VehicleHeightRestrictionJson()
                .type(TypeEnum.VEHICLE_HEIGHT_RESTRICTION)
                .value(METERS)
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.METRE);
    }

    @Test
    void mapperForType() {
        assertThat(mapper.mapperForType()).isEqualTo(RestrictionType.VEHICLE_HEIGHT);
    }
}
