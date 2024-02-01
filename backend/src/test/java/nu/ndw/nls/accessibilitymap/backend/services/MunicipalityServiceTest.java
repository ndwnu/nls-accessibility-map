package nu.ndw.nls.accessibilitymap.backend.services;

import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.MUNICIPALITY;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.MUNICIPALITY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Map;
import nu.ndw.nls.accessibilitymap.backend.config.MunicipalityProperties;
import nu.ndw.nls.accessibilitymap.backend.exceptions.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityServiceTest {

    @Mock
    private MunicipalityProperties municipalityProperties;
    @InjectMocks
    private MunicipalityService municipalityService;

    @Test
    void getMunicipalityById_ok() {
        when(municipalityProperties.getMunicipalities())
                .thenReturn(Map.of(MUNICIPALITY_ID, MUNICIPALITY));
        Municipality municipality = municipalityService.getMunicipalityById(MUNICIPALITY_ID);
        assertThat(municipality).isEqualTo(MUNICIPALITY);
    }

    @Test
    void getMunicipalityById_not_found_exception() {
        when(municipalityProperties.getMunicipalities())
                .thenReturn(Map.of("GM001", MUNICIPALITY));
        MunicipalityNotFoundException municipalityNotFoundException = assertThrows(MunicipalityNotFoundException.class,
                () -> municipalityService.getMunicipalityById(MUNICIPALITY_ID));
        assertThat(municipalityNotFoundException.getMessage())
                .isEqualTo("The municipality with id: GM0307 cannot be found");
    }
}
