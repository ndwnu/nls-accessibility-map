package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.TrafficSignProperties;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeoJsonRoadAsymmetricTrafficSignWriterTest {

    @Mock
    private FileService fileService;

    @Mock
    private FeatureBuilder featureBuilder;

    @Mock
    private GenerateConfiguration generateConfiguration;


    private ExportProperties exportProperties;

    @Mock
    private Accessibility accessibility;

    @Mock
    private RoadSection roadSection;

    @Mock
    private DirectionalSegment directionalSegmentForward1;

    @Mock
    private DirectionalSegment directionalSegmentBackward1;

    @Mock
    private DirectionalSegment directionalSegmentForward2;

    @Mock
    private DirectionalSegment directionalSegmentBackward2;

    @Mock
    private Path exportDirectory;

    @Mock
    private Path exportFile;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private static final FileAttribute<?> FILE_READ_WRITE_PERMISSIONS = PosixFilePermissions.asFileAttribute(Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE)
    );

    @BeforeEach
    void setUp() {

        exportProperties = ExportProperties.builder()
                .name(TrafficSignType.C7.name())
                .accessibilityRequest(AccessibilityRequest.builder().trafficSignTypes(Set.of(TrafficSignType.C7)).build())
                .generateConfiguration(generateConfiguration)
                .includeOnlyTimeWindowedSigns(false)
                .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00"))
                .build();

        roadSection = RoadSection.builder()
                .roadSectionFragments(List.of(
                        RoadSectionFragment.builder()
                                .backwardSegment(directionalSegmentBackward1)
                                .forwardSegment(directionalSegmentForward1)
                                .build(),
                        RoadSectionFragment.builder()
                                .backwardSegment(directionalSegmentBackward2)
                                .forwardSegment(directionalSegmentForward2)
                                .build()
                ))
                .build();

    }

    @SneakyThrows
    @Test
    void writeToFile() {

        Path exportTmpFilePath = Files.createTempFile("tmp", ".tmp", FILE_READ_WRITE_PERMISSIONS);
        try {
            GeoJsonRoadAsymmetricTrafficSignWriter geoJsonRoadAsymmetricTrafficSignWriter = new GeoJsonRoadAsymmetricTrafficSignWriter(
                    fileService,
                    featureBuilder,
                    generateConfiguration,
                    new GeoJsonObjectMapperFactory());
            String expectedFileName = "c7";
            when(directionalSegmentForward1.hasTrafficSigns()).thenReturn(true);
            when(directionalSegmentBackward1.isAccessible()).thenReturn(true);
            when(fileService.createTmpFile(expectedFileName, ".geojson")).thenReturn(exportTmpFilePath);
            when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
            when(generateConfiguration.getGenerationDirectoryPath(exportProperties.startTime()))
                    .thenReturn(exportDirectory);
            when(exportDirectory.resolve(expectedFileName.concat(".geojson"))).thenReturn(exportFile);
            when(exportFile.toAbsolutePath()).thenReturn(Path.of("/tmp/AbstractGeoJsonWriterTest-exportFile.geojson"));
            prepareCreateFeaturesForDirectionalSegment(directionalSegmentForward1);

            geoJsonRoadAsymmetricTrafficSignWriter.export(accessibility, exportProperties);

            assertThatJson(Files.readString(exportTmpFilePath))
                    .isEqualTo("""
                                                        {
                              "features": [
                                {
                                  "id": 11,
                                  "geometry": {
                                    "coordinates": [
                                      [
                                        1.0,
                                        2.0
                                      ]
                                    ],
                                    "type": "LineString"
                                  },
                                  "properties": {
                                    "nwbRoadSectionId": 11,
                                    "accessible": true,
                                    "direction": "FORWARD",
                                    "trafficSignType": "C7",
                                    "windowTimes": "windowTimes",
                                    "iconUrl": "https://example.com/image.png",
                                    "trafficSign": true
                                  },
                                  "type": "Feature"
                                }
                              ],
                              "type": "FeatureCollection"
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
        GeoJsonRoadAsymmetricTrafficSignWriter geoJsonRoadAsymmetricTrafficSignWriter = new GeoJsonRoadAsymmetricTrafficSignWriter(
                fileService,
                featureBuilder,
                generateConfiguration,
                new GeoJsonObjectMapperFactory());
        assertThat(geoJsonRoadAsymmetricTrafficSignWriter.isEnabled(Set.of(ExportType.ASYMMETRIC_TRAFFIC_SIGNS_GEO_JSON))).isTrue();
    }


    private void prepareCreateFeaturesForDirectionalSegment(DirectionalSegment directionalSegmentForward1) {

        when(featureBuilder.createTrafficSigns(
                eq(directionalSegmentForward1),
                any(LongSequenceSupplier.class),
                eq(generateConfiguration))
        ).thenReturn(
                List.of(
                        Feature.builder()
                                .id(11)
                                .geometry(LineStringGeometry.builder()
                                        .coordinates(List.of(List.of(1d, 2d)))
                                        .build())
                                .properties(TrafficSignProperties.builder()
                                        .direction(Direction.FORWARD)
                                        .nwbRoadSectionId(11)
                                        .accessible(true)
                                        .iconUrl(URI.create("https://example.com/image.png"))
                                        .trafficSignType(TrafficSignType.C7)
                                        .windowTimes("windowTimes")
                                        .isTrafficSign(true)
                                        .build())
                                .build()));

    }
}
