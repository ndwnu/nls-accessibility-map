package nu.ndw.nls.accessibilitymap.backend.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import nu.ndw.nls.accessibilitymap.backend.config.MunicipalityProperties;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.MunicipalityMapper;
import nu.ndw.nls.accessibilitymap.backend.services.TestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class MunicipalitiesApiDelegateImplTest {

    @Mock
    private MunicipalityMapper municipalityMapper;
    @Mock
    private MunicipalityProperties municipalityProperties;
    @InjectMocks
    private MunicipalitiesApiDelegateImpl municipalitiesApiDelegate;

    @Test
    void getMunicipalities_ok() {
        var featureCollection = new FeatureCollectionJson();
        when(municipalityProperties.getMunicipalities()).thenReturn(
                Map.of(TestHelper.MUNICIPALITY_ID, TestHelper.MUNICIPALITY));
        when(municipalityMapper.mapToMunicipalitiesToGeoJSON(any())).thenReturn(featureCollection);

        ResponseEntity<FeatureCollectionJson> response = municipalitiesApiDelegate.getMunicipalities();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(featureCollection);
    }
}