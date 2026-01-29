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
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleHeightRestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VehicleHeightRestrictionJsonMapperV2Test {

    private VehicleHeightRestrictionJsonMapperV2 vehicleHeightRestrictionJsonMapperV2;

    @BeforeEach
    void setUp() {
        vehicleHeightRestrictionJsonMapperV2 = new VehicleHeightRestrictionJsonMapperV2();
    }

    @ParameterizedTest
    @CsvSource({
            "125.4, 1.25",
            "125.5, 1.26"})
    void map(double centimeters, double expectedMeters) {

        MaximumRestriction maximumRestriction = MaximumRestriction.builder()
                .value(Maximum.builder().value(centimeters).build())
                .build();

        RestrictionJson restrictionJson = vehicleHeightRestrictionJsonMapperV2.map(maximumRestriction);

        assertThat(restrictionJson)
                .isInstanceOf(VehicleHeightRestrictionJson.class)
                .isEqualTo(getExpected(expectedMeters));
    }

    private VehicleHeightRestrictionJson getExpected(double expectedValue) {

        return new VehicleHeightRestrictionJson()
                .type(TypeEnum.VEHICLE_HEIGHT_RESTRICTION)
                .value(BigDecimal.valueOf(expectedValue))
                .condition(RestrictionConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.METRE);
    }

    @Test
    void getRestrictionType() {

        assertThat(vehicleHeightRestrictionJsonMapperV2.getRestrictionType()).isEqualTo(RestrictionType.VEHICLE_HEIGHT);
    }
}
