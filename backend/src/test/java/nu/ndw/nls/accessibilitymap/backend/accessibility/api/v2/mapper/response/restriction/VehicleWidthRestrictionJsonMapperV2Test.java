package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleWidthRestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VehicleWidthRestrictionJsonMapperV2Test {

    private VehicleWidthRestrictionJsonMapperV2 vehicleWidthRestrictionJsonMapperV2;

    @BeforeEach
    void setUp() {
        vehicleWidthRestrictionJsonMapperV2 = new VehicleWidthRestrictionJsonMapperV2();
    }

    @ParameterizedTest
    @CsvSource({
            "125.4, 1.25",
            "125.5, 1.26"})
    void map(double centimeters, double expectedMeters) {

        MaximumRestriction maximumRestriction = MaximumRestriction.builder()
                .value(Maximum.builder().value(centimeters).build())
                .build();

        RestrictionJson restrictionJson = vehicleWidthRestrictionJsonMapperV2.map(maximumRestriction);

        assertThat(restrictionJson)
                .isInstanceOf(VehicleWidthRestrictionJson.class)
                .isEqualTo(getExpected(expectedMeters));
    }

    private VehicleWidthRestrictionJson getExpected(double expectedValue) {

        return new VehicleWidthRestrictionJson()
                .type(TypeEnum.VEHICLE_WIDTH_RESTRICTION)
                .value(BigDecimal.valueOf(expectedValue))
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.METRE);
    }

    @Test
    void getRestrictionType() {

        assertThat(vehicleWidthRestrictionJsonMapperV2.getRestrictionType()).isEqualTo(RestrictionType.VEHICLE_WIDTH);
    }
}
