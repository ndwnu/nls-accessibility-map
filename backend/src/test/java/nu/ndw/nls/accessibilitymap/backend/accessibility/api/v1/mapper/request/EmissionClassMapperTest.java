package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionClassJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class EmissionClassMapperTest {

    private EmissionClassMapper emissionClassMapper;

    @BeforeEach
    void setUp() {
        emissionClassMapper = new EmissionClassMapper();
    }

    @ParameterizedTest
    @NullSource
    void mapFuelType_shouldReturnNullWhenMappingNullFuelTypeJson(EmissionClassJson emissionClassJson) {
        Set<EmissionClass> result = emissionClassMapper.map(emissionClassJson);
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @EnumSource(EmissionClassJson.class)
    void mapFuelType_AllSupportedFuelTypeJsonValues(EmissionClassJson emissionClassJson) {

        Set<EmissionClass> result = emissionClassMapper.map(emissionClassJson);

        EmissionClass expectedEmissionClass = switch (emissionClassJson) {
            case ZERO -> null;
            case EURO_1 -> EmissionClass.EURO_1;
            case EURO_2 -> EmissionClass.EURO_2;
            case EURO_3 -> EmissionClass.EURO_3;
            case EURO_4 -> EmissionClass.EURO_4;
            case EURO_5 -> EmissionClass.EURO_5;
            case EURO_6 -> EmissionClass.EURO_6;
        };

        if (Objects.isNull(expectedEmissionClass)) {
            assertThat(result).isEmpty();
        } else {
            assertThat(result).containsExactly(expectedEmissionClass);
        }
    }
}
