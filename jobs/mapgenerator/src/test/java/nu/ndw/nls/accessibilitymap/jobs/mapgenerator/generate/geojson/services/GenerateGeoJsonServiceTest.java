package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateGeoJsonServiceTest {

    @Mock
    private GenerateConfiguration generateConfiguration;

    @InjectMocks
    private GenerateGeoJsonService generateGeoJsonService;

    @Mock
    private GeoJsonProperties geoJsonProperties;


    @Test
    void generate() {
//        when(generateConfiguration.getConfiguration(GenerateGeoJsonType.C6)).thenReturn(geoJsonProperties);
//
//        generateGeoJsonService.generate(GenerateGeoJsonType.C6);
//
//        verify(generateConfiguration).getConfiguration(GenerateGeoJsonType.C6);
    }
}