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
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleHeightReasonJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleHeightReasonJsonMapperV2Test {

    private VehicleHeightReasonJsonMapperV2 vehicleHeightReasonJsonMapperV2;

    @Mock
    private RestrictionJson restrictionJson;
    @BeforeEach
    void setUp() {
        vehicleHeightReasonJsonMapperV2 = new VehicleHeightReasonJsonMapperV2();
    }

    @ParameterizedTest
    @CsvSource({
            "125.4, 1.25",
            "125.5, 1.26"})
    void map(double centimeters, double expectedMeters) {

        MaximumReason maximumReason = MaximumReason.builder()
                .value(Maximum.builder().value(centimeters).build())
                .build();

        ReasonJson reasonJson = vehicleHeightReasonJsonMapperV2.map(maximumReason, List.of(restrictionJson));

        assertThat(reasonJson)
                .isInstanceOf(VehicleHeightReasonJson.class)
                .isEqualTo(getExpected(expectedMeters));
    }

    private VehicleHeightReasonJson getExpected(double expectedValue) {

        return new VehicleHeightReasonJson()
                .type(TypeEnum.VEHICLE_HEIGHT_REASON)
                .value(BigDecimal.valueOf(expectedValue))
                .condition(ReasonConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.METRE)
                .becauseOf(List.of(restrictionJson));
    }

    @Test
    void getReasonType() {

        assertThat(vehicleHeightReasonJsonMapperV2.getReasonType()).isEqualTo(ReasonType.VEHICLE_HEIGHT);
    }
}
