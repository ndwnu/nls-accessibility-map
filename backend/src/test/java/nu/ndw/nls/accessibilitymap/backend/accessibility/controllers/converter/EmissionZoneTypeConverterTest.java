package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.converter;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionZoneTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneTypeConverterTest {

    private EmissionZoneTypeConverter emissionZoneTypeConverter;

    @BeforeEach
    void setUp() {

        emissionZoneTypeConverter = new EmissionZoneTypeConverter();
    }

    @ParameterizedTest
    @EnumSource(EmissionZoneTypeJson.class)
    void convert_null(EmissionZoneTypeJson emissionZoneTypeJson) {

        assertThat(emissionZoneTypeConverter.convert(emissionZoneTypeJson.getValue())).isEqualTo(emissionZoneTypeJson);
    }
}