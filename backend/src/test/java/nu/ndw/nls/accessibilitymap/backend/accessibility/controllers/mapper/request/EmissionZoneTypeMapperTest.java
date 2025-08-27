package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionZoneTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneTypeMapperTest {


    private EmissionZoneTypeMapper emissionZoneTypeMapper;

    @BeforeEach
    void setUp() {

        emissionZoneTypeMapper = new EmissionZoneTypeMapper();
    }

    @ParameterizedTest
    @EnumSource(EmissionZoneTypeJson.class)
    @NullSource
    void mapEmissionZoneType(EmissionZoneTypeJson emissionZoneTypeJson) {

        EmissionZoneType expectedEmissionZone = map(emissionZoneTypeJson);

        assertThat(emissionZoneTypeMapper.mapEmissionZoneType(emissionZoneTypeJson)).isEqualTo(expectedEmissionZone);
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
