package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services;

import static nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType.LINE_STRING_GEO_JSON;
import static nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType.POLYGON_GEO_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers.GeoJsonPolygonWriter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers.GeoJsonRoadSectionWriter;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapGeneratorServiceTest {

    private static final Set<ExportType> EXPORT_TYPES = Set.of(POLYGON_GEO_JSON, LINE_STRING_GEO_JSON);

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
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private Accessibility accessibility;

    @Mock
    private NlsEvent nlsEvent;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private final OffsetDateTime timestamp = OffsetDateTime.now();

    private ExportProperties exportProperties;

    private List<RoadSection> roadSections;

    @BeforeEach
    void setUp() {

        exportProperties = ExportProperties.builder()
                .startTime(timestamp)
                .exportTypes(EXPORT_TYPES)
                .nwbVersion(2)
                .accessibilityRequest(accessibilityRequest)
                .publishEvents(true)
                .build();

        mapGeneratorService = new MapGeneratorService(
                accessibilityGeoJsonGeneratedEventMapper,
                List.of(geoJsonPolygonWriter, geoJsonRoadSectionWriter),
                accessibilityService,
                messageService
        );

        roadSections = List.of(
                RoadSection.builder()
                        .roadSectionFragments(List.of(
                                RoadSectionFragment.builder()
                                        .backwardSegment(
                                                DirectionalSegment.builder()
                                                        .trafficSigns(List.of(TrafficSign.builder().build()))
                                                        .build()
                                        )
                                        .forwardSegment(
                                                DirectionalSegment.builder()
                                                        .trafficSigns(List.of(TrafficSign.builder().build()))
                                                        .build()
                                        )
                                        .build()
                        ))
                        .build()
        );

    }

    @Test
    void generate() {

        when(geoJsonPolygonWriter.isEnabled(EXPORT_TYPES)).thenReturn(true);
        when(geoJsonRoadSectionWriter.isEnabled(EXPORT_TYPES)).thenReturn(true);
        when(accessibilityService.calculateAccessibility(accessibilityRequest))
                .thenReturn(accessibility);
        when(accessibility.combinedAccessibility()).thenReturn(roadSections);

        when(accessibilityGeoJsonGeneratedEventMapper.map(
                exportProperties.accessibilityRequest().trafficSignTypes().stream().toList(),
                -1,
                exportProperties.nwbVersion(),
                timestamp.toInstant()))
                .thenReturn(nlsEvent);

        mapGeneratorService.generate(exportProperties);

        verify(geoJsonRoadSectionWriter).export(accessibility, exportProperties);
        verify(geoJsonPolygonWriter).export(accessibility, exportProperties);
        verify(messageService).publish(nlsEvent);

        loggerExtension.containsLog(
                Level.INFO,
                "Generating with the following properties: %s".formatted(exportProperties));
        loggerExtension.containsLog(
                Level.DEBUG,
                "Found 2 with road section fragments with traffic signs.");
    }

    @Test
    void generate_doNotPublishEvents() {

        when(geoJsonPolygonWriter.isEnabled(EXPORT_TYPES)).thenReturn(true);
        when(geoJsonRoadSectionWriter.isEnabled(EXPORT_TYPES)).thenReturn(true);
        exportProperties = exportProperties.withPublishEvents(false);
        when(accessibilityService.calculateAccessibility(accessibilityRequest))
                .thenReturn(accessibility);
        when(accessibility.combinedAccessibility()).thenReturn(roadSections);

        mapGeneratorService.generate(exportProperties);

        verify(geoJsonRoadSectionWriter).export(accessibility, exportProperties);
        verify(geoJsonPolygonWriter).export(accessibility, exportProperties);
        verifyNoMoreInteractions(messageService);

        loggerExtension.containsLog(
                Level.INFO,
                "Generating with the following properties: %s".formatted(exportProperties));
        loggerExtension.containsLog(
                Level.DEBUG,
                "Found 2 with road section fragments with traffic signs.");
    }

    @Test
    void generate_validAnnotation() {

        AnnotationUtil.methodParameterContainsAnnotation(
                mapGeneratorService.getClass(),
                Valid.class,
                "generate",
                "exportProperties",
                annotation -> assertThat(annotation).isNotNull());
    }
}
