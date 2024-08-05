package nu.ndw.nls.accessibilitymap.accessibility.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.exceptions.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.accessibility.model.Municipality;
import nu.ndw.nls.accessibilitymap.accessibility.municipality.MunicipalityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityServiceTest {
    private static final String MUNICIPALITY_ID_STRING = "GM0307";
    private static final String MUNICIPALITY_ID_2_STRING = "GM0008";

    @Mock
    private MunicipalityConfiguration municipalityConfiguration;

    @Mock
    private Municipality municipality;

    @InjectMocks
    private MunicipalityService municipalityService;

    @Test
    void getMunicipalityById_ok() {
        when(municipalityConfiguration.getMunicipalities())
                .thenReturn(Map.of(MUNICIPALITY_ID_STRING, municipality));
        Municipality result = municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING);
        assertThat(result).isEqualTo(municipality);
    }

    @Test
    void getMunicipalityById_exception_notFound() {
        when(municipalityConfiguration.getMunicipalities())
                .thenReturn(Map.of(MUNICIPALITY_ID_2_STRING, municipality));
        MunicipalityNotFoundException municipalityNotFoundException = assertThrows(MunicipalityNotFoundException.class,
                () -> municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING));
        assertThat(municipalityNotFoundException.getMessage())
                .isEqualTo("The municipality with id: GM0307 cannot be found");
    }
}
