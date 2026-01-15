package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleWeightRestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VehicleWeightRestrictionJsonMapperV2Test {

    private VehicleWeightRestrictionJsonMapperV2 vehicleWeightRestrictionJsonMapperV2;

    @BeforeEach
    void setUp() {
        vehicleWeightRestrictionJsonMapperV2 = new VehicleWeightRestrictionJsonMapperV2();
    }

    @ParameterizedTest
    @CsvSource({
            "124, 0.12",
            "125, 0.13"})
    void map(double kiloGrams, double expectedTons) {
        MaximumRestriction maximumRestriction = MaximumRestriction.builder()
                .value(Maximum.builder().value(kiloGrams).build())
                .build();

        RestrictionJson restrictionJson = vehicleWeightRestrictionJsonMapperV2.map(maximumRestriction);

        assertThat(restrictionJson)
                .isInstanceOf(VehicleWeightRestrictionJson.class)
                .isEqualTo(getExpected(expectedTons));
    }

    private VehicleWeightRestrictionJson getExpected(double expectedValue) {

        return new VehicleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_WEIGHT_RESTRICTION)
                .value(BigDecimal.valueOf(expectedValue))
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS);
    }

    @Test
    void getRestrictionType() {

        assertThat(vehicleWeightRestrictionJsonMapperV2.getRestrictionType()).isEqualTo(RestrictionType.VEHICLE_WEIGHT);
    }
}
