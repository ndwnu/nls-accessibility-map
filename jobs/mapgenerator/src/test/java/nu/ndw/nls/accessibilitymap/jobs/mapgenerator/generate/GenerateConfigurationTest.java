package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith( MockitoExtension.class )
class GenerateConfigurationTest {

    @Mock
    private GenerateProperties generateProperties;

    @InjectMocks
    private GenerateConfiguration generateConfiguration;

    @Mock
    private Map<GenerateGeoJsonType, GeoJsonProperties> typeToGeoJsonProperties;

    @Mock
    private GeoJsonProperties geoJsonProperties;

    @Test
    void getConfiguration_ok() {
        when(generateProperties.getGeojson()).thenReturn(typeToGeoJsonProperties);
        when(typeToGeoJsonProperties.get(GenerateGeoJsonType.C6)).thenReturn(geoJsonProperties);
        assertEquals(geoJsonProperties, generateConfiguration.getConfiguration(GenerateGeoJsonType.C6));
    }
}