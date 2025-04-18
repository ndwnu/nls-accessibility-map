package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class EmissionClassMapperTest {

    private EmissionClassMapper emissionClassMapper;

    @BeforeEach
    void setUp() {
        emissionClassMapper = new EmissionClassMapper();
    }

    @Test
    void mapEmissionClassification_shouldThrowNotSupportedException() {

        EmissionClassJson mockEmissionClass = Mockito.mock(EmissionClassJson.class);
        when(mockEmissionClass.name()).thenReturn("unsupported emission type");

        assertThatThrownBy(() -> emissionClassMapper.mapEmissionClass(mockEmissionClass))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid emission classification type: unsupported emission type");
    }

    @Test
    void mapFuelType_shouldReturnNullWhenMappingNullFuelTypeJson() {
        Set<EmissionClass> result = emissionClassMapper.mapEmissionClass(null);
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @EnumSource(EmissionClassJson.class)
    void mapFuelType_AllSupportedFuelTypeJsonValues(EmissionClassJson emissionClassJson) {

        Set<EmissionClass> result = emissionClassMapper.mapEmissionClass(emissionClassJson);

        assertThat(result).containsExactly((EmissionClass.valueOf(emissionClassJson.name())));
    }
}
