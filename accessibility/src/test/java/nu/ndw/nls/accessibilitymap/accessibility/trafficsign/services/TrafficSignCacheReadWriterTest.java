package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static nu.ndw.nls.springboot.test.logging.dto.VerificationMode.times;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.javacrumbs.jsonunit.core.Option;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignCacheReadWriterTest {

    private TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    @Mock
    private ClockService clockService;

    private TrafficSign trafficSign1;

    private TrafficSign trafficSign2;

    private Path testDir;

    private Path cacheDir;

    private TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

    @BeforeEach
    void setUp() throws IOException {

        trafficSign1 = TrafficSign.builder()
                .id(1)
                .externalId("externalId1")
                .trafficSignType(TrafficSignType.C6)
                .blackCode(1D)
                .textSigns(List.of(TextSign.builder()
                        .type(TextSignType.TIME_PERIOD)
                        .text("signText")
                        .build()))
                .iconUri(URI.create("http://some.uri/icon.png"))
                .trafficSignOrderUrl(URI.create("http://some.uri/order.html"))
                .latitude(1.1)
                .longitude(2.2)
                .direction(Direction.FORWARD)
                .fraction(0.5)
                .roadSectionId(2)
                .restrictions(Restrictions.builder()
                        .transportTypes(Set.of(TransportType.CAR))
                        .vehicleAxleLoadInKg(Maximum.builder().value(10d).build())
                        .vehicleHeightInCm(Maximum.builder().value(20d).build())
                        .vehicleLengthInCm(Maximum.builder().value(30d).build())
                        .vehicleWidthInCm(Maximum.builder().value(40d).build())
                        .vehicleWeightInKg(Maximum.builder().value(50d).build())
                        .build())
                .build();
        trafficSign2 = trafficSign1.withId(2).withExternalId("externalId2");

        testDir = Files.createTempDirectory("testDir");
        cacheDir = testDir.resolve("cache");

        trafficSignCacheConfiguration = TrafficSignCacheConfiguration.builder()
                .folder(cacheDir)
                .fileNameActiveVersion("active")
                .failOnNoDataOnStartup(false)
                .build();

        trafficSignCacheReadWriter = new TrafficSignCacheReadWriter(trafficSignCacheConfiguration, new ObjectMapper(), clockService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void read() throws JsonProcessingException {

        when(clockService.now()).thenReturn(NOW);

        TrafficSigns trafficSigns = new TrafficSigns(List.of(trafficSign1, trafficSign2));
        trafficSignCacheReadWriter.write(trafficSigns);

        Optional<TrafficSigns> cachedTrafficSigns = trafficSignCacheReadWriter.read();
        assertThat(cachedTrafficSigns).isPresent();

        ObjectMapper objectMapper = new ObjectMapper();
        assertThatJson(objectMapper.writeValueAsString(trafficSigns))
                .isEqualTo(objectMapper.writeValueAsString(cachedTrafficSigns.get()));
    }

    @Test
    void read_failed() throws IOException {

        when(clockService.now()).thenReturn(NOW);

        TrafficSigns trafficSigns = new TrafficSigns(List.of(trafficSign1, trafficSign2));

        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(trafficSignCacheConfiguration.getActiveVersion(), TrafficSigns.class))
                .thenThrow(new IOException("some error"));
        trafficSignCacheReadWriter = new TrafficSignCacheReadWriter(trafficSignCacheConfiguration, objectMapper, clockService);
        trafficSignCacheReadWriter.write(trafficSigns);

        Optional<TrafficSigns> cachedTrafficSigns = trafficSignCacheReadWriter.read();
        assertThat(cachedTrafficSigns).isEmpty();
        loggerExtension.containsLog(Level.ERROR, "Failed to read traffic signs from file", "some error");
    }

    @Test
    void read_failed_stopOnStartup() throws IOException {

        trafficSignCacheConfiguration.setFailOnNoDataOnStartup(true);
        when(clockService.now()).thenReturn(NOW);

        TrafficSigns trafficSigns = new TrafficSigns(List.of(trafficSign1, trafficSign2));

        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(trafficSignCacheConfiguration.getActiveVersion(), TrafficSigns.class))
                .thenThrow(new IOException("some error"));
        trafficSignCacheReadWriter = new TrafficSignCacheReadWriter(trafficSignCacheConfiguration, objectMapper, clockService);
        trafficSignCacheReadWriter.write(trafficSigns);

        assertThat(catchThrowable(() -> trafficSignCacheReadWriter.read()))
                .isInstanceOf(IllegalStateException.class);
        loggerExtension.containsLog(Level.ERROR, "Failed to read traffic signs from file", "some error");
    }

    @Test
    void write() throws IOException {

        File activeFile = trafficSignCacheConfiguration.getActiveVersion();
        when(clockService.now()).thenReturn(NOW);

        assertThat(cacheDir).doesNotExist();
        assertThat(activeFile.toPath()).doesNotExist();

        TrafficSigns trafficSigns = new TrafficSigns(List.of(trafficSign1, trafficSign2));

        trafficSignCacheReadWriter.write(trafficSigns);

        assertThat(cacheDir).exists();
        assertThat(activeFile.toPath()).exists();

        Path expectedTrafficSignFile = cacheDir.resolve(
                "trafficSigns-%s.json".formatted(NOW.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        assertThat(expectedTrafficSignFile).exists();
        assertThat(activeFile.toPath().toRealPath().toAbsolutePath())
                .isEqualTo(expectedTrafficSignFile.toRealPath().toAbsolutePath());

        assertThatJson(FileUtils.readFileToString(activeFile, StandardCharsets.UTF_8))
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(new ObjectMapper().writeValueAsString(List.of(trafficSign1, trafficSign2)));

        loggerExtension.containsLog(Level.INFO, "Writing traffic signs to file: %s".formatted(expectedTrafficSignFile.toAbsolutePath()));
        loggerExtension.containsLog(Level.INFO, "Updated symlink: %s".formatted(activeFile.toPath().toAbsolutePath()));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void write_failed() {

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.createDirectories(trafficSignCacheConfiguration.getFolder())).thenThrow(new IOException("some error"));

            trafficSignCacheReadWriter.write(new TrafficSigns());

            loggerExtension.containsLog(Level.ERROR, "Failed to write traffic signs to file", "some error");
        }
    }

    @Test
    void write_replaceSymLink() throws IOException {

        File activeFile = trafficSignCacheConfiguration.getActiveVersion();

        TrafficSigns trafficSigns = new TrafficSigns(List.of(trafficSign1, trafficSign2));

        // First run
        when(clockService.now()).thenReturn(NOW);
        trafficSignCacheReadWriter.write(trafficSigns);

        // Second run. recreate Symlink to new file
        OffsetDateTime newTimestamp = NOW.plusMinutes(1);
        when(clockService.now()).thenReturn(newTimestamp);
        trafficSignCacheReadWriter.write(trafficSigns);

        Path oldTrafficSignFile = cacheDir.resolve("trafficSigns-%s.json".formatted(NOW.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        Path newTrafficSignFile = cacheDir.resolve(
                "trafficSigns-%s.json".formatted(newTimestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

        assertThat(oldTrafficSignFile).doesNotExist();
        assertThat(newTrafficSignFile).exists();
        assertThat(activeFile.toPath().toRealPath().toAbsolutePath())
                .isEqualTo(newTrafficSignFile.toRealPath().toAbsolutePath());

        assertThatJson(FileUtils.readFileToString(activeFile, StandardCharsets.UTF_8))
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(new ObjectMapper().writeValueAsString(List.of(trafficSign1, trafficSign2)));

        loggerExtension.containsLog(Level.INFO, "Writing traffic signs to file: %s".formatted(oldTrafficSignFile.toAbsolutePath()));
        loggerExtension.containsLog(Level.INFO, "Removed old symlink target: %s".formatted(oldTrafficSignFile.toAbsolutePath()));
        loggerExtension.containsLog(Level.INFO, "Writing traffic signs to file: %s".formatted(newTrafficSignFile.toAbsolutePath()));
        loggerExtension.containsLog(Level.INFO, "Updated symlink: %s".formatted(activeFile.toPath().toAbsolutePath()), List.of(), times(2));
    }

    @Test
    void write_replaceSymLink_invalidExistingSymlink() throws IOException {

        File activeFile = trafficSignCacheConfiguration.getActiveVersion();
        Files.createDirectories(activeFile.toPath().getParent());

        // Create faulty symlink
        Files.createSymbolicLink(activeFile.toPath(), Path.of("some-non-existing-file"));

        // Second run. recreate Symlink to new file
        OffsetDateTime newTimestamp = NOW.plusMinutes(1);
        when(clockService.now()).thenReturn(newTimestamp);

        TrafficSigns trafficSigns = new TrafficSigns(List.of(trafficSign1, trafficSign2));

        trafficSignCacheReadWriter.write(trafficSigns);

        Path oldTrafficSignFile = cacheDir.resolve("trafficSigns-%s.json".formatted(NOW.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        Path newTrafficSignFile = cacheDir.resolve(
                "trafficSigns-%s.json".formatted(newTimestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

        assertThat(oldTrafficSignFile).doesNotExist();
        assertThat(newTrafficSignFile).exists();
        assertThat(activeFile.toPath().toRealPath().toAbsolutePath())
                .isEqualTo(newTrafficSignFile.toRealPath().toAbsolutePath());

        loggerExtension.containsLog(Level.INFO, "Writing traffic signs to file: %s".formatted(newTrafficSignFile.toAbsolutePath()));
        loggerExtension.containsLog(Level.INFO, "Updated symlink: %s".formatted(activeFile.toPath().toAbsolutePath()));
    }
}
