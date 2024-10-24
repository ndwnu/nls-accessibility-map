package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.mapper.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers.GeoJsonPolygonWriter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers.GeoJsonRoadSectionWriter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.AnnotationUtil;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.LoggerExtension;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapGeneratorServiceTest {

    private MapGeneratorService mapGeneratorService;

    @Mock
    private AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    @Mock
    private GeoJsonPolygonWriter geoJsonPolygonWriter;
    @Mock
    private GeoJsonRoadSectionWriter geoJsonRoadSectionWriter;

    @Mock
    private AccessibilityService accessibilityService;

    @Mock
    private MessageService messageService;

    @Mock
    private AccessibilityRequestMapper accessibilityRequestMapper;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private Accessibility accessibility;

    @Mock
    private NlsEvent nlsEvent;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private OffsetDateTime timestamp = OffsetDateTime.now();

    private GeoGenerationProperties geoGenerationProperties;

    private List<RoadSection> roadSections;

    @BeforeEach
    void setUp() {

        geoGenerationProperties = GeoGenerationProperties.builder()
                .startTime(timestamp)
                .exportVersion(1)
                .nwbVersion(2)
                .trafficSignType(TrafficSignType.C7)
                .publishEvents(true)
                .build();

        mapGeneratorService = new MapGeneratorService(
                accessibilityGeoJsonGeneratedEventMapper,
                geoJsonRoadSectionWriter,
                geoJsonPolygonWriter,
                accessibilityService,
                messageService,
                accessibilityRequestMapper
        );

        roadSections = List.of(
                RoadSection.builder()
                        .roadSectionFragments(List.of(
                                RoadSectionFragment.builder()
                                        .backwardSegment(
                                                DirectionalSegment.builder()
                                                        .trafficSign(TrafficSign.builder().build())
                                                        .build()
                                        )
                                        .forwardSegment(
                                                DirectionalSegment.builder()
                                                        .trafficSign(TrafficSign.builder().build())
                                                        .build()
                                        )
                                        .build()
                        ))
                        .build()
        );

    }

    @Test
    void generate_ok() {

        when(accessibilityRequestMapper.map(geoGenerationProperties)).thenReturn(accessibilityRequest);
        when(accessibilityService.calculateAccessibility(accessibilityRequest)).thenReturn(accessibility);
        when(accessibility.combinedAccessibility()).thenReturn(roadSections);

        when(accessibilityGeoJsonGeneratedEventMapper.map(
                geoGenerationProperties.trafficSignType(),
                geoGenerationProperties.exportVersion(),
                geoGenerationProperties.nwbVersion(),
                timestamp.toInstant()))
                .thenReturn(nlsEvent);

        mapGeneratorService.generate(geoGenerationProperties);

        verify(geoJsonRoadSectionWriter).writeToFile(accessibility, geoGenerationProperties);
        verify(geoJsonPolygonWriter).writeToFile(accessibility, geoGenerationProperties);
        verify(messageService).publish(nlsEvent);

        loggerExtension.containsLog(
                Level.INFO,
                "Generating with the following properties: %s".formatted(geoGenerationProperties));
        loggerExtension.containsLog(
                Level.DEBUG,
                "Found 2 with road sections with traffic signs.");
    }

    @Test
    void generate_ok_doNotPublishEvents() {

        geoGenerationProperties = geoGenerationProperties.withPublishEvents(false);

        when(accessibilityRequestMapper.map(geoGenerationProperties)).thenReturn(accessibilityRequest);
        when(accessibilityService.calculateAccessibility(accessibilityRequest)).thenReturn(accessibility);
        when(accessibility.combinedAccessibility()).thenReturn(roadSections);

        mapGeneratorService.generate(geoGenerationProperties);

        verify(geoJsonRoadSectionWriter).writeToFile(accessibility, geoGenerationProperties);
        verify(geoJsonPolygonWriter).writeToFile(accessibility, geoGenerationProperties);
        verifyNoMoreInteractions(messageService);

        loggerExtension.containsLog(
                Level.INFO,
                "Generating with the following properties: %s".formatted(geoGenerationProperties));
        loggerExtension.containsLog(
                Level.DEBUG,
                "Found 2 with road sections with traffic signs.");
    }

    @Test
    void generate_validAnnotation() {

        AnnotationUtil.methodParameterContainsAnnotation(
                mapGeneratorService.getClass(),
                Valid.class,
                "generate",
                "geoGenerationProperties",
                annotation -> assertThat(annotation).isNotNull());
    }
}