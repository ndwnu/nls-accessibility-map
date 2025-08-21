package nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
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
    @EnumSource(nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZoneType.class)
    @NullSource
    void map(nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZoneType emissionZoneType) {

        EmissionZoneType expectedEmissionZoneType = map_emissionZoneType(emissionZoneType);

        EmissionZoneType actualEmissionZoneType = emissionZoneTypeMapper.map(emissionZoneType);

        assertThat(actualEmissionZoneType).isEqualTo(expectedEmissionZoneType);
    }

    public EmissionZoneType map_emissionZoneType(
            nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZoneType emissionZoneType) {

        if (Objects.isNull(emissionZoneType)) {
            return EmissionZoneType.UNKNOWN;
        }

        return switch (emissionZoneType) {
            case ZERO_EMISSION_ZONE -> EmissionZoneType.ZERO;
            case LOW_EMISSION_ZONE -> EmissionZoneType.LOW;
            default -> EmissionZoneType.UNKNOWN;
        };
    }
}