package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.EmissionZoneTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneTypeMapperV2Test {


    private EmissionZoneTypeMapperV2 emissionZoneTypeMapperV2;

    @BeforeEach
    void setUp() {

        emissionZoneTypeMapperV2 = new EmissionZoneTypeMapperV2();
    }

    @ParameterizedTest
    @EnumSource(EmissionZoneTypeJson.class)
    @NullSource
    void mapEmissionZoneType(EmissionZoneTypeJson emissionZoneTypeJson) {

        EmissionZoneType expectedEmissionZone = map(emissionZoneTypeJson);

        assertThat(emissionZoneTypeMapperV2.map(emissionZoneTypeJson)).isEqualTo(expectedEmissionZone);
    }

    public EmissionZoneType map(EmissionZoneTypeJson emissionZoneTypeJson) {

        if (Objects.isNull(emissionZoneTypeJson)) {
            return null;
        } else {
            return switch (emissionZoneTypeJson) {
                case LOW_EMISSION_ZONE -> EmissionZoneType.LOW;
                case ZERO_EMISSION_ZONE -> EmissionZoneType.ZERO;
            };
        }
    }
}
