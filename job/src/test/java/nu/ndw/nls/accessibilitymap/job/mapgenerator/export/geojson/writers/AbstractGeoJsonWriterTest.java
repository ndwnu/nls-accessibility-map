package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.writers;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.util.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import java.io.File;
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
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.FeatureCollection;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractGeoJsonWriterTest {

    private AbstractGeoJsonWriter abstractGeoJsonWriter;

    @Mock
    private GeoJsonJsonMapperFactory geoJsonJsonMapperFactory;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @Mock
    private FileService fileService;

    @Mock
    private Accessibility accessibility;

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

    @ParameterizedTest
    @EnumSource(SupplementarySignTypeWindowTimeSet.class)
    void export(SupplementarySignTypeWindowTimeSet supplementarySignTypeWindowTimeSet) throws IOException {

        when(geoJsonJsonMapperFactory.create(generateConfiguration)).thenReturn(new JsonMapper());
        Path exportTmpFilePath = Files.createTempFile("tmp", ".tmp", FILE_READ_WRITE_PERMISSIONS);

        ExportProperties exportProperties = buildExportProperties(supplementarySignTypeWindowTimeSet);

        try {
            String expectedFileName = "c7".concat(supplementarySignTypeWindowTimeSet.includeWindowTimes()
                    ? "WindowTimeSegments"
                    : "");

            when(fileService.createTmpFile(expectedFileName, ".geojson")).thenReturn(exportTmpFilePath);
            when(generateConfiguration.getGenerationDirectoryPath(exportProperties.startTime()))
                    .thenReturn(exportDirectory);
            when(exportFile.toAbsolutePath()).thenReturn(Path.of("/tmp/AbstractGeoJsonWriterTest-exportFile.geojson"));
            when(exportDirectory.resolve(expectedFileName.concat(".geojson"))).thenReturn(exportFile);

            abstractGeoJsonWriter = new TestGeoJsonWriter(
                    generateConfiguration,
                    geoJsonJsonMapperFactory,
                    fileService,
                    accessibility,
                    exportProperties);
            abstractGeoJsonWriter.export(accessibility, exportProperties);

            assertThatJson(Files.readString(exportTmpFilePath))
                    .isEqualTo("""
                            {
                               "features":[
                                  {
                                     "id":1,
                                     "type":"Feature",
                                     "geometry":null,
                                     "properties":null
                                  }
                               ],
                               "type":"FeatureCollection"
                            }
                            """);

            verify(fileService).moveFileAndOverride(exportTmpFilePath, exportFile);

            loggerExtension.containsLog(Level.DEBUG, "Started generating geojson");
            loggerExtension.containsLog(Level.DEBUG, "Started building features");
            loggerExtension.containsLog(Level.DEBUG,
                    "Started writing geojson to temp file: %s".formatted(exportTmpFilePath));
            loggerExtension.containsLog(Level.DEBUG,
                    "Moving geojson to: /tmp/AbstractGeoJsonWriterTest-exportFile.geojson");
        } finally {
            Files.deleteIfExists(exportTmpFilePath);
        }
    }

    @Test
    void export_ioException() throws IOException {

        JsonMapper jsonMapper = mock(JsonMapper.class);
        doThrow(JacksonException.class).when(jsonMapper).writeValue(any(File.class), any(Object.class));
        when(geoJsonJsonMapperFactory.create(generateConfiguration)).thenReturn(jsonMapper);

        Path exportTmpFilePath = Files.createTempFile("tmp", ".tmp", FILE_READ_WRITE_PERMISSIONS);

        ExportProperties exportProperties = buildExportProperties(SupplementarySignTypeWindowTimeSet.NULL);

        try {
            String expectedFileName = "c7";

            when(fileService.createTmpFile(expectedFileName, ".geojson")).thenReturn(exportTmpFilePath);

            abstractGeoJsonWriter = new TestGeoJsonWriter(
                    generateConfiguration,
                    geoJsonJsonMapperFactory,
                    fileService,
                    accessibility,
                    exportProperties);

            assertThat(catchThrowable(() -> abstractGeoJsonWriter.export(accessibility, exportProperties)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(
                            "Failed to serialize geojson to file: %s".formatted(exportTmpFilePath.toAbsolutePath()));

            verify(fileService, never()).moveFileAndOverride(any(), any());

            loggerExtension.containsLog(Level.DEBUG, "Started generating geojson");
            loggerExtension.containsLog(Level.DEBUG,
                    "Started writing geojson to temp file: %s".formatted(exportTmpFilePath));
        } finally {
            Files.deleteIfExists(exportTmpFilePath);
        }
    }

    private static class TestGeoJsonWriter extends AbstractGeoJsonWriter {

        private final Accessibility accessibility;

        private final ExportProperties exportProperties;

        public TestGeoJsonWriter(
                GenerateConfiguration generateConfiguration,
                GeoJsonJsonMapperFactory geoJsonJsonMapperFactory,
                FileService fileService,
                Accessibility accessibility,
                ExportProperties exportProperties) {

            super(generateConfiguration, geoJsonJsonMapperFactory, fileService);
            this.accessibility = accessibility;
            this.exportProperties = exportProperties;
        }

        @Override
        public boolean isEnabled(Set<ExportType> exportTypes) {

            return exportTypes.contains(ExportType.LINE_STRING_GEO_JSON);
        }

        @Override
        protected FeatureCollection prepareGeoJsonFeatureCollection(Accessibility accessibility,
                ExportProperties exportProperties, AtomicLong idSequenceSupplier) {

            assertThat(accessibility).isEqualTo(this.accessibility);
            assertThat(exportProperties).isEqualTo(this.exportProperties);
            assertThat(accessibility).isNotNull();

            return FeatureCollection.builder()
                    .features(List.of(
                            Feature.builder()
                                    .id(1)
                                    .build()
                    ))
                    .build();
        }
    }

    private ExportProperties buildExportProperties(SupplementarySignTypeWindowTimeSet supplementarySignTypeWindowTimeSet) {
        return ExportProperties.builder()
                .name(TrafficSignType.C7.name())
                .accessibilityRequest(AccessibilityRequest.builder()
                        .trafficSignTypes(Set.of(TrafficSignType.C7))
                        .trafficSignSupplementarySignTypes(supplementarySignTypeWindowTimeSet.getSupplementarySignTypes())
                        .build())
                .generateConfiguration(generateConfiguration)
                .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00"))
                .build();
    }

    @Getter
    @RequiredArgsConstructor
    enum SupplementarySignTypeWindowTimeSet {
        WINDOW_TIMES(SupplementarySignType.getWindowTimeTypes()),
        NULL(null),
        EMPTY_SET(Collections.emptySet());

        private final Set<SupplementarySignType> supplementarySignTypes;

        public boolean includeWindowTimes() {
            return this == WINDOW_TIMES;
        }
    }

}
