package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.configuration.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
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
        when(generateConfiguration.getConfiguration(TrafficSignType.C6)).thenReturn(geoJsonProperties);
        when(generateConfiguration.getGenerateProperties()).thenReturn(generateProperties);
        when(generateProperties.getRootGenerationDestination()).thenReturn(DESTINATION_PATH);
        when(geoJsonProperties.getPathDatePattern())
                .thenReturn("'/v1/windowTimes/'yyyyMMdd'/geojson/c6WindowTimeSegments.geojson'");

        assertEquals(DESTINATION_PATH.resolve("/v1/windowTimes/20240612/geojson/c6WindowTimeSegments.geojson"),
                blobStorageLocationMapper.map(TrafficSignType.C6, LocalDate.of(2024, 6, 12)));

        verify(generateConfiguration).getConfiguration(TrafficSignType.C6);
        verify(geoJsonProperties).getPathDatePattern();
    }
}