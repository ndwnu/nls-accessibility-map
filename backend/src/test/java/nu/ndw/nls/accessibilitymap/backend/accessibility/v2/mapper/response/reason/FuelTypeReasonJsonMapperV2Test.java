package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeReason;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.FuelTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FuelTypeReasonJsonMapperV2Test {

    private FuelTypeReasonJsonMapperV2 fuelTypeReasonJsonMapperV2;

    @Mock
    private FuelTypeMapperV2 fuelTypeMapperV2;

    @Mock
    private FuelType fuelType;

    @Mock
    private FuelTypeJson fuelTypeJson;

    @Mock
    private RestrictionJson restrictionJson;

    @BeforeEach
    void setUp() {
        fuelTypeReasonJsonMapperV2 = new FuelTypeReasonJsonMapperV2(fuelTypeMapperV2);
    }

    @Test
    void map() {
        FuelTypeReason fuelTypeReason = FuelTypeReason.builder()
                .value(Set.of(fuelType))
                .build();

        when(fuelTypeMapperV2.map(fuelType)).thenReturn(fuelTypeJson);

        ReasonJson reasonJson = fuelTypeReasonJsonMapperV2.map(fuelTypeReason, List.of(restrictionJson));

        assertThat(reasonJson)
                .isInstanceOf(FuelTypeReasonJson.class)
                .isEqualTo(getExpected());
    }

    private FuelTypeReasonJson getExpected() {
        return new FuelTypeReasonJson()
                .type(TypeEnum.FUEL_TYPE_REASON)
                .values(List.of(fuelTypeJson))
                .condition(ReasonConditionJson.EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.ENUM)
                .becauseOf(List.of(restrictionJson));
    }

    @Test
    void getReasonType() {
        assertThat(fuelTypeReasonJsonMapperV2.getReasonType()).isEqualTo(ReasonType.FUEL_TYPE);
    }
}
