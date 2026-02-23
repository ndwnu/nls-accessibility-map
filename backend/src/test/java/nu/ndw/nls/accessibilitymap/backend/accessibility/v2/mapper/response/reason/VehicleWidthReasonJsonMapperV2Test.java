package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumReason;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleWidthReasonJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleWidthReasonJsonMapperV2Test {

    private VehicleWidthReasonJsonMapperV2 vehicleWidthReasonJsonMapperV2;

    @Mock
    private RestrictionJson restrictionJson;

    @BeforeEach
    void setUp() {
        vehicleWidthReasonJsonMapperV2 = new VehicleWidthReasonJsonMapperV2();
    }

    @ParameterizedTest
    @CsvSource({
            "125.4, 1.25",
            "125.5, 1.26"})
    void map(double centimeters, double expectedMeters) {

        MaximumReason maximumReason = MaximumReason.builder()
                .value(Maximum.builder().value(centimeters).build())
                .build();

        ReasonJson reasonJson = vehicleWidthReasonJsonMapperV2.map(maximumReason, List.of(restrictionJson));

        assertThat(reasonJson)
                .isInstanceOf(VehicleWidthReasonJson.class)
                .isEqualTo(getExpected(expectedMeters));
    }

    private VehicleWidthReasonJson getExpected(double expectedValue) {

        return new VehicleWidthReasonJson()
                .type(TypeEnum.VEHICLE_WIDTH_REASON)
                .value(BigDecimal.valueOf(expectedValue))
                .condition(ReasonConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.METRE)
                .becauseOf(List.of(restrictionJson));
    }

    @Test
    void getReasonType() {

        assertThat(vehicleWidthReasonJsonMapperV2.getReasonType()).isEqualTo(ReasonType.VEHICLE_WIDTH);
    }
}
