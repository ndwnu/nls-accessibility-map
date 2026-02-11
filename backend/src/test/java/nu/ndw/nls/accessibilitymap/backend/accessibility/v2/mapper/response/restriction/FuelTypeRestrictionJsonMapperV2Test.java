package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.restriction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.FuelTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FuelTypeRestrictionJsonMapperV2Test {

    private FuelTypeRestrictionJsonMapperV2 fuelTypeRestrictionJsonMapperV2;

    @Mock
    private FuelTypeMapperV2 fuelTypeMapperV2;

    @Mock
    private FuelType fuelType;

    @Mock
    private FuelTypeJson fuelTypeJson;

    @BeforeEach
    void setUp() {
        fuelTypeRestrictionJsonMapperV2 = new FuelTypeRestrictionJsonMapperV2(fuelTypeMapperV2);
    }

    @Test
    void map() {
        FuelTypeRestriction fuelTypeRestriction = FuelTypeRestriction.builder()
                .value(Set.of(fuelType))
                .build();

        when(fuelTypeMapperV2.map(fuelType)).thenReturn(fuelTypeJson);

        RestrictionJson restrictionJson = fuelTypeRestrictionJsonMapperV2.map(fuelTypeRestriction);

        assertThat(restrictionJson)
                .isInstanceOf(FuelTypeRestrictionJson.class)
                .isEqualTo(getExpected());
    }

    private FuelTypeRestrictionJson getExpected() {
        return new FuelTypeRestrictionJson()
                .type(TypeEnum.FUEL_TYPE_RESTRICTION)
                .values(List.of(fuelTypeJson))
                .condition(RestrictionConditionJson.EQUALS)
                .unitSymbol(RestrictionUnitSymbolJson.ENUM);
    }

    @Test
    void getRestrictionType() {
        assertThat(fuelTypeRestrictionJsonMapperV2.getRestrictionType()).isEqualTo(RestrictionType.FUEL_TYPE);
    }
}
