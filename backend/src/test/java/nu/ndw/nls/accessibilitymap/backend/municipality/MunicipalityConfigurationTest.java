package nu.ndw.nls.accessibilitymap.backend.municipality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import nu.ndw.nls.accessibilitymap.backend.municipality.mappers.MunicipalityMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityConfigurationTest {

    private static final String MUNICIPALITY_NAME_A = "a";

    private static final String MUNICIPALITY_NAME_B = "b";

    @Mock
    private MunicipalityMapper municipalityMapper;

    @Mock
    private MunicipalityProperties municipalityProperties;

    @Mock
    private MunicipalityProperty municipalityPropertyA;

    @Mock
    private MunicipalityProperty municipalityPropertyB;

    @Mock
    private Municipality municipalityA;

    @Mock
    private Municipality municipalityB;

    @Test
    void construction() {
        when(municipalityProperties.getMunicipalities()).thenReturn(Map.of(MUNICIPALITY_NAME_A, municipalityPropertyA,
                MUNICIPALITY_NAME_B, municipalityPropertyB));

        when(municipalityMapper.map(municipalityPropertyA)).thenReturn(municipalityA);
        when(municipalityMapper.map(municipalityPropertyB)).thenReturn(municipalityB);

        MunicipalityConfiguration municipalityConfiguration = new MunicipalityConfiguration(municipalityMapper,
                municipalityProperties);

        assertEquals(Map.of(MUNICIPALITY_NAME_A, municipalityA,
                MUNICIPALITY_NAME_B, municipalityB), municipalityConfiguration.getMunicipalities());
    }
}
