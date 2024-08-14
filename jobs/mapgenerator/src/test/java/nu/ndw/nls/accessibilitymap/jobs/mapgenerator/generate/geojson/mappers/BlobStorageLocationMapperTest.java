package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlobStorageLocationMapperTest {


    private static final Path DESTINATION_PATH = Path.of("destination-path");
    @Mock
    private GenerateConfiguration generateConfiguration;

    @InjectMocks
    private BlobStorageLocationMapper blobStorageLocationMapper;

    @Mock
    private GeoJsonProperties geoJsonProperties;

    @Mock
    private GenerateProperties generateProperties;



    @Test
    void map_ok() {
        when(generateConfiguration.getConfiguration(GenerateGeoJsonType.C6)).thenReturn(geoJsonProperties);
        when(generateConfiguration.getGenerateProperties()).thenReturn(generateProperties);
        when(generateProperties.getRootGenerationDestination()).thenReturn(DESTINATION_PATH);
        when(geoJsonProperties.getPathDatePattern())
                .thenReturn("'/api/v1/windowTimes/'yyyyMMdd'/geojson/c6WindowTimeSegments.geojson'");

        assertEquals(DESTINATION_PATH.resolve("/api/v1/windowTimes/20240612/geojson/c6WindowTimeSegments.geojson"),
                blobStorageLocationMapper.map(GenerateGeoJsonType.C6, LocalDate.of(2024, 6, 12)));

        verify(generateConfiguration).getConfiguration(GenerateGeoJsonType.C6);
        verify(geoJsonProperties).getPathDatePattern();
    }
}