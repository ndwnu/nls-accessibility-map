package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.writers;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.util.polygon.MultiPolygonFactory;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeoJsonPolygonWriterTest {

    @Mock
    private MultiPolygonFactory multiPolygonFactory;

    @Mock
    private FileService fileService;

    @Mock
    private FeatureBuilder featureBuilder;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @Mock
    private Accessibility accessibility;

    private RoadSection roadSection;

    @Mock
    private LineString lineStringDoesNotIntersect;

    @Mock
    private LineString lineStringDoesIntersects;

    @Mock
    private Path exportDirectory;

    @Mock
    private Path exportFile;

    @Mock
    private MultiPolygon multiPolygon;

    @Mock
    private Geometry geometry1;

    @Mock
    private Geometry geometry2;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private static final FileAttribute<?> FILE_READ_WRITE_PERMISSIONS = PosixFilePermissions.asFileAttribute(Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE)
    );

    private Set<Restriction> relevantRestrictions;

    private Set<Long> relevantRoadSectionIds;

    @BeforeEach
    void setUp() {

        roadSection = RoadSection.builder()
                .id(1L)
                .roadSectionFragments(List.of(
                        RoadSectionFragment.builder()
                                .backwardSegment(DirectionalSegment.builder()
                                        .lineString(lineStringDoesIntersects)
                                        .restrictions(List.of(TrafficSign.builder()
                                                .id(1)
                                                .textSigns(List.of(
                                                        TextSign.builder()
                                                                .type(TextSignType.TIME_PERIOD)
                                                                .text("window1")
                                                                .build(),
                                                        TextSign.builder()
                                                                .type(TextSignType.TIME_PERIOD)
                                                                .text("window2")
                                                                .build()))
                                                .build()))
                                        .build())
                                .forwardSegment(DirectionalSegment.builder()
                                        .lineString(lineStringDoesIntersects)
                                        .restrictions(List.of(TrafficSign.builder()
                                                .id(2)
                                                .textSigns(List.of(
                                                        TextSign.builder()
                                                                .type(TextSignType.FREE_TEXT)
                                                                .text("window3")
                                                                .build(),
                                                        TextSign.builder()
                                                                .type(TextSignType.TIME_PERIOD)
                                                                .text("window4")
                                                                .build()))
                                                .build()))
                                        .build())
                                .build(),
                        RoadSectionFragment.builder()
                                .backwardSegment(DirectionalSegment.builder()
                                        .lineString(lineStringDoesNotIntersect)
                                        .restrictions(null)
                                        .build())
                                .forwardSegment(DirectionalSegment.builder()
                                        .lineString(lineStringDoesIntersects)
                                        .restrictions(List.of(TrafficSign.builder()
                                                .id(3)
                                                .textSigns(List.of(
                                                        TextSign.builder()
                                                                .type(TextSignType.FREE_TEXT)
                                                                .text("window5")
                                                                .build(),
                                                        TextSign.builder()
                                                                .type(TextSignType.TIME_PERIOD)
                                                                .text("window6")
                                                                .build()))
                                                .build()))
                                        .build())
                                .build(),
                        RoadSectionFragment.builder()
                                .forwardSegment(DirectionalSegment.builder()
                                        .lineString(lineStringDoesNotIntersect)
                                        .restrictions(List.of(TrafficSign.builder()
                                                .id(4)
                                                .textSigns(List.of(
                                                        TextSign.builder()
                                                                .type(TextSignType.FREE_TEXT)
                                                                .text("window5")
                                                                .build(),
                                                        TextSign.builder()
                                                                .type(TextSignType.TIME_PERIOD)
                                                                .text("window6")
                                                                .build()))
                                                .build()))
                                        .build())
                                .build()
                ))
                .build();

        roadSection.getRoadSectionFragments().forEach(roadSectionFragment -> {
            roadSectionFragment.setRoadSection(roadSection);
            roadSectionFragment.getSegments().forEach(directionalSegment -> directionalSegment.setRoadSectionFragment(roadSectionFragment));
        });

        relevantRoadSectionIds = Set.of(roadSection.getId());
        relevantRestrictions = Set.of(
                roadSection.getRoadSectionFragments().getFirst().getForwardSegment().getRestrictions().getFirst(),
                roadSection.getRoadSectionFragments().getFirst().getBackwardSegment().getRestrictions().getFirst(),
                roadSection.getRoadSectionFragments().get(1).getForwardSegment().getRestrictions().getFirst()
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false
            """)
    void prepareGeoJsonFeatureCollection(boolean includeOnlyTimeWindowedSigns) throws IOException {

        Path exportTmpFilePath = Files.createTempFile("tmp", ".tmp", FILE_READ_WRITE_PERMISSIONS);

        ExportProperties exportProperties = buildExportProperties(includeOnlyTimeWindowedSigns);

        try {
            String expectedFileName = "c7%s-polygon".formatted(includeOnlyTimeWindowedSigns ? "WindowTimeSegments" : "");

            GeoJsonPolygonWriter geoJsonPolygonWriter = new GeoJsonPolygonWriter(
                    fileService,
                    generateConfiguration,
                    new GeoJsonObjectMapperFactory(),
                    multiPolygonFactory,
                    featureBuilder);

            when(fileService.createTmpFile(expectedFileName, ".geojson")).thenReturn(exportTmpFilePath);
            when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
            when(generateConfiguration.getGenerationDirectoryPath(exportProperties.startTime()))
                    .thenReturn(exportDirectory);
            when(exportDirectory.resolve(expectedFileName.concat(".geojson"))).thenReturn(exportFile);
            when(exportFile.toAbsolutePath()).thenReturn(Path.of("/tmp/AbstractGeoJsonWriterTest-exportFile.geojson"));
            when(multiPolygonFactory.createMultiPolygon(roadSection.getRoadSectionFragments(), 0.0005))
                    .thenReturn(multiPolygon);

            when(multiPolygon.getNumGeometries()).thenReturn(2);
            when(multiPolygon.getGeometryN(0)).thenReturn(geometry1);
            when(multiPolygon.getGeometryN(1)).thenReturn(geometry2);

            when(geometry1.intersects(lineStringDoesIntersects)).thenReturn(true);
            when(geometry1.intersects(lineStringDoesNotIntersect)).thenReturn(false);
            when(geometry2.intersects(lineStringDoesIntersects)).thenReturn(true);
            when(geometry2.intersects(lineStringDoesNotIntersect)).thenReturn(false);

            when(featureBuilder.createPolygon(
                    eq(geometry1),
                    any(AtomicLong.class),
                    eq(relevantRestrictions),
                    eq(relevantRoadSectionIds)))
                    .thenReturn(Feature.builder().id(1).build());

            when(featureBuilder.createPolygon(
                    eq(geometry2),
                    any(AtomicLong.class),
                    eq(relevantRestrictions),
                    eq(relevantRoadSectionIds)))
                    .thenReturn(Feature.builder().id(2).build());

            geoJsonPolygonWriter.export(accessibility, exportProperties);

            assertThatJson(Files.readString(exportTmpFilePath))
                    .isEqualTo("""
                            {
                               "features":[
                                  {
                                     "id":1,
                                     "type":"Feature"
                                  },
                                  {
                                     "id":2,
                                     "type":"Feature"
                                  }
                               ],
                               "type":"FeatureCollection"
                            }
                            """);
            verify(fileService).moveFileAndOverride(exportTmpFilePath, exportFile);

            loggerExtension.containsLog(Level.DEBUG, "Started generating geojson");
            loggerExtension.containsLog(Level.DEBUG, "Started building features");
            loggerExtension.containsLog(
                    Level.DEBUG,
                    "Started writing geojson to temp file: %s".formatted(exportTmpFilePath));
            loggerExtension.containsLog(
                    Level.DEBUG,
                    "Moving geojson to: /tmp/AbstractGeoJsonWriterTest-exportFile.geojson");
        } finally {
            Files.deleteIfExists(exportTmpFilePath);
        }
    }

    @Test
    void isEnabled() {
        GeoJsonPolygonWriter geoJsonPolygonWriter = new GeoJsonPolygonWriter(
                fileService,
                generateConfiguration,
                new GeoJsonObjectMapperFactory(),
                multiPolygonFactory,
                featureBuilder);

        assertThat(geoJsonPolygonWriter.isEnabled(Set.of(ExportType.POLYGON_GEO_JSON))).isTrue();
    }

    private ExportProperties buildExportProperties(boolean includeOnlyTimeWindowedSigns) {

        return ExportProperties.builder()
                .name(TrafficSignType.C7.name())
                .accessibilityRequest(AccessibilityRequest.builder()
                        .trafficSignTypes(Set.of(TrafficSignType.C7))
                        .trafficSignTextSignTypes(
                                includeOnlyTimeWindowedSigns
                                        ? Set.of(TextSignType.TIME_PERIOD) : null)
                        .build())
                .generateConfiguration(generateConfiguration)
                .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00"))
                .polygonMaxDistanceBetweenPoints(0.0005)
                .build();
    }
}
