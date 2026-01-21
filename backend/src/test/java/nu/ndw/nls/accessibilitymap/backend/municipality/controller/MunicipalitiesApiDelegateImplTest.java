package nu.ndw.nls.accessibilitymap.backend.municipality.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.municipality.controller.mapper.MunicipalityFeatureMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.MunicipalityFeatureCollectionJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class MunicipalitiesApiDelegateImplTest {

    private static final String MUNICIPALITY_ID_STRING = "GM0307";

    private static final String MUNICIPALITY_ID_2_STRING = "GM0008";

    private static final String MUNICIPALITY_ID_2 = "GM0008";

    private static final String MUNICIPALITY_ID = "GM0307";

    @Mock
    private MunicipalityFeatureMapper municipalityFeatureMapper;

    @Mock
    private MunicipalityService municipalityService;

    @Mock
    private Municipality municipality1;

    @Mock
    private Municipality municipality2;

    @Mock
    private MunicipalityFeatureCollectionJson featureCollection;

    @InjectMocks
    private MunicipalitiesApiDelegateImpl municipalitiesApiDelegate;

    @Test
    void getMunicipalities() {

        when(municipalityService.findAll()).thenReturn(List.of(municipality1, municipality2));
        when(municipality1.id()).thenReturn(MUNICIPALITY_ID);
        when(municipality2.id()).thenReturn(MUNICIPALITY_ID_2);
        when(municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(List.of(municipality2, municipality1))).thenReturn(featureCollection);

        ResponseEntity<MunicipalityFeatureCollectionJson> response = municipalitiesApiDelegate.getMunicipalities();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(featureCollection);
    }
}
