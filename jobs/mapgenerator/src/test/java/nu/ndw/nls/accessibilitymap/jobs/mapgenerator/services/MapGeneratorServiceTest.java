package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Valid;
import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.mapper.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers.OutputWriter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util.AnnotationUtil;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private AccessibilityRequestMapper accessibilityRequestMapper;

    @BeforeEach
    void setUp() {

        mapGeneratorService = new MapGeneratorService(
                accessibilityGeoJsonGeneratedEventMapper,
                outputWriters,
                accessibilityService,
                messageService,
                accessibilityRequestMapper,
				clockService
        );
    }

    @Test
    void generate_ok() {

        mapGeneratorService.generate(GeoGenerationProperties.builder().build());
    }


    @Test
    void generate_validAnnotation() {

        AnnotationUtil.methodParameterContainsAnnotation(
                mapGeneratorService.getClass(),
                Valid.class,
                "generate",
                "mapGenerationProperties",
                annotation -> assertThat(annotation).isNotNull());
    }


}