package nu.ndw.nls.accessibilitymap.backend.municipality.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.municipality.controller.mapper.MunicipalityFeatureMapperV2;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.generated.model.v2.MunicipalityFeatureCollectionJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class MunicipalitiesV2ApiDelegateImplTest {

    @Mock
    private MunicipalityFeatureMapperV2 municipalityFeatureMapper;

    @Mock
    private MunicipalityService municipalityService;

    @Mock
    private Municipality municipality1;

    @Mock
    private Municipality municipality2;

    @Mock
    private MunicipalityFeatureCollectionJson featureCollection;

    @InjectMocks
    private MunicipalitiesV2ApiDelegateImpl municipalitiesApiDelegate;

    @Test
    void getMunicipalities() {

        when(municipalityService.findAll()).thenReturn(List.of(municipality1, municipality2));
        when(municipality1.id()).thenReturn("GM0307");
        when(municipality2.id()).thenReturn("GM0008");
        when(municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(List.of(municipality2, municipality1))).thenReturn(featureCollection);

        ResponseEntity<MunicipalityFeatureCollectionJson> response = municipalitiesApiDelegate.getMunicipalities();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(featureCollection);
    }
}
