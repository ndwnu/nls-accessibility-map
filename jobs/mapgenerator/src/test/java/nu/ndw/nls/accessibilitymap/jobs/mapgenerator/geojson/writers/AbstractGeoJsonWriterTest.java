package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

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
import com.fasterxml.jackson.databind.ObjectMapper;
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
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.LoggerExtension;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.LongSequenceSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractGeoJsonWriterTest {

    private AbstractGeoJsonWriter abstractGeoJsonWriter;

    @Mock
    private GeoJsonObjectMapperFactory geoJsonObjectMapperFactory;

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

    private GeoGenerationProperties geoGenerationProperties;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private static final FileAttribute<?> FILE_READ_WRITE_PERMISSIONS = PosixFilePermissions.asFileAttribute(Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE)
    );

    @BeforeEach
    void setUp() {

        geoGenerationProperties = GeoGenerationProperties.builder()
                .trafficSignType(TrafficSignType.C7)
                .generateConfiguration(generateConfiguration)
                .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00"))
                .build();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false
            """)
    void writeToFile_ok(boolean includeOnlyTimeWindowedSigns) throws IOException {

        when(geoJsonObjectMapperFactory.create(generateConfiguration)).thenReturn(new ObjectMapper());
        Path exportTmpFilePath = Files.createTempFile("tmp", ".tmp", FILE_READ_WRITE_PERMISSIONS);

        geoGenerationProperties = geoGenerationProperties.withIncludeOnlyTimeWindowedSigns(
                includeOnlyTimeWindowedSigns);
        try {
            String expectedFileName = "c7".concat(includeOnlyTimeWindowedSigns ? "WindowTimeSegments" : "");

            when(fileService.createTmpFile(expectedFileName, ".geojson")).thenReturn(exportTmpFilePath);
            when(generateConfiguration.getGenerationDirectoryPath(geoGenerationProperties.startTime()))
                    .thenReturn(exportDirectory);
            when(exportFile.toAbsolutePath()).thenReturn(Path.of("/tmp/AbstractGeoJsonWriterTest-exportFile.geojson"));
            when(exportDirectory.resolve(expectedFileName.concat(".geojson"))).thenReturn(exportFile);

            abstractGeoJsonWriter = new TestGeoJsonWriter(
                    generateConfiguration,
                    geoJsonObjectMapperFactory,
                    fileService,
                    accessibility,
                    geoGenerationProperties);
            abstractGeoJsonWriter.writeToFile(accessibility, geoGenerationProperties);

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
            loggerExtension.containsLog(Level.DEBUG, "Started writing geojson to temp file: %s".formatted(exportTmpFilePath));
            loggerExtension.containsLog(Level.DEBUG, "Moving geojson to: /tmp/AbstractGeoJsonWriterTest-exportFile.geojson");

        } finally {
            Files.deleteIfExists(exportTmpFilePath);
        }
    }

    @Test
    void writeToFile_ioException() throws IOException {

        ObjectMapper objectMapper = mock(ObjectMapper.class);
        doThrow(new IOException("some exception")).when(objectMapper).writeValue(any(File.class), any(Object.class));
        when(geoJsonObjectMapperFactory.create(generateConfiguration)).thenReturn(objectMapper);

        Path exportTmpFilePath = Files.createTempFile("tmp", ".tmp", FILE_READ_WRITE_PERMISSIONS);

        geoGenerationProperties = geoGenerationProperties.withIncludeOnlyTimeWindowedSigns(false);

        try {
            String expectedFileName = "c7";

            when(fileService.createTmpFile(expectedFileName, ".geojson")).thenReturn(exportTmpFilePath);

            abstractGeoJsonWriter = new TestGeoJsonWriter(
                    generateConfiguration,
                    geoJsonObjectMapperFactory,
                    fileService,
                    accessibility,
                    geoGenerationProperties);

            assertThat(catchThrowable(() -> abstractGeoJsonWriter.writeToFile(accessibility, geoGenerationProperties)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Failed to serialize geojson to file: %s".formatted(exportTmpFilePath.toAbsolutePath()));

            verify(fileService, never()).moveFileAndOverride(any(), any());

            loggerExtension.containsLog(Level.DEBUG, "Started generating geojson");
            loggerExtension.containsLog(Level.DEBUG, "Started writing geojson to temp file: %s".formatted(exportTmpFilePath));
        } finally {
            Files.deleteIfExists(exportTmpFilePath);
        }
    }

    private static class TestGeoJsonWriter extends AbstractGeoJsonWriter {

        private final Accessibility accessibility;

        private final GeoGenerationProperties geoGenerationProperties;

        public TestGeoJsonWriter(
                GenerateConfiguration generateConfiguration,
                GeoJsonObjectMapperFactory geoJsonObjectMapperFactory,
                FileService fileService,
                Accessibility accessibility,
                GeoGenerationProperties geoGenerationProperties) {

            super(generateConfiguration, geoJsonObjectMapperFactory, fileService);
            this.accessibility = accessibility;
            this.geoGenerationProperties = geoGenerationProperties;
        }

        @Override
        protected FeatureCollection prepareGeoJsonFeatureCollection(Accessibility accessibility,
                GeoGenerationProperties geoGenerationProperties, LongSequenceSupplier idSequenceSupplier) {

            assertThat(accessibility).isEqualTo(this.accessibility);
            assertThat(geoGenerationProperties).isEqualTo(this.geoGenerationProperties);
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
}