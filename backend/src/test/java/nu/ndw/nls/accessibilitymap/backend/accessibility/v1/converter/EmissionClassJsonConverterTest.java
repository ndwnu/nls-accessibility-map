package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.converter;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionClassJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionClassJsonConverterTest {

    private EmissionClassJsonConverter emissionClassJsonConverter;

    @BeforeEach
    void setUp() {

        emissionClassJsonConverter = new EmissionClassJsonConverter();
    }

    @ParameterizedTest
    @EnumSource(EmissionClassJson.class)
    void convert_null(EmissionClassJson emissionClassJson) {

        assertThat(emissionClassJsonConverter.convert(emissionClassJson.getValue())).isEqualTo(emissionClassJson);
    }
}
