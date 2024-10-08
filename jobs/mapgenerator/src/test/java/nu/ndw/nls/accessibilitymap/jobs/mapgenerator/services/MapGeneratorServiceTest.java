package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers.OutputWriter;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapGeneratorServiceTest {

    private MapGeneratorService mapGeneratorService;

    private AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    private List<OutputWriter> outputWriters;

    private AccessibilityService accessibilityService;

    private MessageService messageService;

    private ClockService clockService;

    @BeforeEach
    void setUp() {

        mapGeneratorService = new MapGeneratorService(
                accessibilityGeoJsonGeneratedEventMapper,
                outputWriters,
                accessibilityService,
                messageService,
				clockService
        );
    }


}