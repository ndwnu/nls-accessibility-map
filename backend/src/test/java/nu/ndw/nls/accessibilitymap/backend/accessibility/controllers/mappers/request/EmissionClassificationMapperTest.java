package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClassification;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EmissionClassificationMapperTest {

    private EmissionClassificationMapper emissionClassificationMapper;

    @BeforeEach
    void setUp() {
        emissionClassificationMapper = new EmissionClassificationMapper();
    }

    @Test
    void mapFuelType_shouldReturnNullWhenMappingNullFuelTypeJson() {
        EmissionClassJson nullEmissionClassJson = null;
        Set<EmissionClassification> result = emissionClassificationMapper.mapEmissionClassification(nullEmissionClassJson);
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @EnumSource(EmissionClassJson.class)
    void mapFuelType_AllSupportedFuelTypeJsonValues(EmissionClassJson emissionClassJson) {
        Set<EmissionClassification> result = emissionClassificationMapper.mapEmissionClassification(emissionClassJson);
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .contains(EmissionClassification.valueOf(emissionClassJson.name()));
    }

}
