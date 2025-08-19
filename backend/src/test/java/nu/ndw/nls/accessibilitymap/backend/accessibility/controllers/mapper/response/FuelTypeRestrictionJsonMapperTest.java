package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.FuelTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionUnitSymbolJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FuelTypeRestrictionJsonMapperTest {

    @Mock
    private FuelTypeRestriction fuelTypeRestriction;
    @Mock
    private FuelTypeMapper fuelTypeMapper;
    @Mock
    private FuelType fuelType;
    @Mock
    private FuelTypeJson fuelTypeJson;

    @InjectMocks
    private FuelTypeRestrictionJsonMapper mapper;

    @Test
    void mapToRestrictionJson() {
        when(fuelTypeRestriction.getValue()).thenReturn(Set.of(fuelType));
        when(fuelTypeMapper.mapFuelTypeJson(fuelType)).thenReturn(fuelTypeJson);
        RestrictionJson actual = mapper.mapToRestrictionJson(fuelTypeRestriction);

        assertThat(actual)
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
    void mapperForType() {
        assertThat(mapper.mapperForType()).isEqualTo(RestrictionType.FUEL_TYPE);
    }
}