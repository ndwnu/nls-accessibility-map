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
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleWeightReasonJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleWeightReasonJsonMapperV2Test {

    private VehicleWeightReasonJsonMapperV2 vehicleWeightReasonJsonMapperV2;

    @Mock
    private RestrictionJson restrictionJson;

    @BeforeEach
    void setUp() {
        vehicleWeightReasonJsonMapperV2 = new VehicleWeightReasonJsonMapperV2();
    }

    @ParameterizedTest
    @CsvSource({
            "124, 0.12",
            "125, 0.13"})
    void map(double kiloGrams, double expectedTons) {
        MaximumReason maximumReason = MaximumReason.builder()
                .value(Maximum.builder().value(kiloGrams).build())
                .build();

        ReasonJson reasonJson = vehicleWeightReasonJsonMapperV2.map(maximumReason, List.of(restrictionJson));

        assertThat(reasonJson)
                .isInstanceOf(VehicleWeightReasonJson.class)
                .isEqualTo(getExpected(expectedTons));
    }

    private VehicleWeightReasonJson getExpected(double expectedValue) {

        return new VehicleWeightReasonJson()
                .type(TypeEnum.VEHICLE_WEIGHT_REASON)
                .value(BigDecimal.valueOf(expectedValue))
                .condition(ReasonConditionJson.GREATER_THAN_OR_EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.TONS)
                .becauseOf(List.of(restrictionJson));
    }

    @Test
    void getReasonType() {

        assertThat(vehicleWeightReasonJsonMapperV2.getReasonType()).isEqualTo(ReasonType.VEHICLE_WEIGHT);
    }
}
