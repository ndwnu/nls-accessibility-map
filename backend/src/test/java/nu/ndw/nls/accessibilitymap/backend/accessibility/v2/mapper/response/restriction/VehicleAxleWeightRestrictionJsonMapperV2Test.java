package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.restriction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleAxleWeightRestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VehicleAxleWeightRestrictionJsonMapperV2Test {

    private VehicleAxleWeightRestrictionJsonMapperV2 vehicleAxleWeightRestrictionJsonMapperV2;

    @BeforeEach
    void setUp() {
        vehicleAxleWeightRestrictionJsonMapperV2 = new VehicleAxleWeightRestrictionJsonMapperV2();
    }

    @ParameterizedTest
    @CsvSource({
            "124, 0.12",
            "125, 0.13"})
    void map(double kiloGrams, double expectedTons) {
        MaximumRestriction maximumRestriction = MaximumRestriction.builder()
                .value(Maximum.builder().value(kiloGrams).build())
                .build();

        RestrictionJson restrictionJson = vehicleAxleWeightRestrictionJsonMapperV2.map(maximumRestriction);

        assertThat(restrictionJson)
                .isInstanceOf(VehicleAxleWeightRestrictionJson.class)
                .isEqualTo(getExpected(expectedTons));
    }

    private VehicleAxleWeightRestrictionJson getExpected(double expectedValue) {

        return new VehicleAxleWeightRestrictionJson()
                .type(TypeEnum.VEHICLE_AXLE_WEIGHT_RESTRICTION)
                .value(BigDecimal.valueOf(expectedValue))
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.TONS);
    }

    @Test
    void getRestrictionType() {

        assertThat(vehicleAxleWeightRestrictionJsonMapperV2.getRestrictionType()).isEqualTo(RestrictionType.VEHICLE_AXLE_LOAD);
    }
}
