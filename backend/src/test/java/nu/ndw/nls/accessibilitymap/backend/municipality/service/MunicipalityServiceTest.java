package nu.ndw.nls.accessibilitymap.backend.municipality.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.backend.exception.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.MunicipalityRepository;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityServiceTest {

    private static final String MUNICIPALITY_ID_STRING = "GM0307";

    @Mock
    private MunicipalityRepository repository;

    @Mock
    private Municipality municipality;

    @InjectMocks
    private MunicipalityService municipalityService;

    @Test
    void getMunicipalityById() {

        when(repository.findFirstById(MUNICIPALITY_ID_STRING)).thenReturn(Optional.of(municipality));

        Municipality result = municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING);

        assertThat(result).isEqualTo(municipality);
    }

    @Test
    void getMunicipalityById_exception_notFound() {

        when(repository.findFirstById(MUNICIPALITY_ID_STRING)).thenReturn(Optional.empty());

        MunicipalityNotFoundException municipalityNotFoundException = assertThrows(MunicipalityNotFoundException.class,
                () -> municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING));

        assertThat(municipalityNotFoundException.getMessage())
                .isEqualTo("The municipality with id: %s cannot be found".formatted(MUNICIPALITY_ID_STRING));
    }

    @Test
    void findAll() {

        when(repository.findAll()).thenReturn(List.of(municipality));

        List<Municipality> municipalities = municipalityService.findAll();

        assertThat(municipalities).containsExactly(municipality);
    }

    @Test
    void findAll_nothingFound() {

        when(repository.findAll()).thenReturn(List.of());

        List<Municipality> municipalities = municipalityService.findAll();

        assertThat(municipalities).isEmpty();
    }
}
