package nu.ndw.nls.accessibilitymap.accessibility.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.logging.dto.VerificationMode;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@ExtendWith(MockitoExtension.class)
class CacheTest {

    @Mock
    private ClockService clockService;

    private Path testDir;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @Mock
    private Object data;

    private CacheConfiguration cacheConfiguration;

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());

        cacheConfiguration = CacheConfiguration.builder()
                .name("testCache")
                .folder(testDir.resolve("testFolder"))
                .fileNameActiveVersion("active")
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void read() throws IOException {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getActiveVersion().toPath());

        Cache<Object> cache = new Cache<>(cacheConfiguration, clockService) {
            private int counter;

            @Override
            protected Object readData(Path activeVersion) {
                if (counter++ == 0) {
                    return null;
                }
                return data;
            }

            @Override
            protected void writeData(Path target, Object data) {
                // Do nothing
            }
        };

        assertThat(cache.get()).isNull();

        cache.read();

        assertThat(cache.get()).isEqualTo(data);

        loggerExtension.containsLog(
                Level.INFO,
                "Reading %s from location: %s".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getActiveVersion().getAbsolutePath()),
                VerificationMode.times(2));
        loggerExtension.containsLog(
                Level.INFO,
                "Read testCache data from `%s` with size 0.00MB in 310 ms".formatted(cacheConfiguration.getActiveVersion()
                        .getAbsolutePath()),
                VerificationMode.times(2));
    }

    @Test
    void read_error() throws IOException {

        Files.createDirectories(cacheConfiguration.getActiveVersion().toPath());

        cacheConfiguration.setAcceptableConsequentReadFailures(1);
        cacheConfiguration.setFailOnCacheReadError(false);

        Cache<Object> cache = new Cache<>(cacheConfiguration, clockService) {
            @Override
            protected Object readData(Path activeVersion) {
                throw new RuntimeException("test");
            }

            @Override
            protected void writeData(Path target, Object data) {
                // Do nothing
            }
        };

        cache.read();

        assertThat(cache.get()).isNull();
        loggerExtension.containsLog(
                Level.ERROR,
                "Failed to read %s".formatted(cacheConfiguration.getName()),
                "test");

        cache.read();

        loggerExtension.containsLog(
                Level.ERROR,
                "Failed to read %s".formatted(cacheConfiguration.getName()),
                "test",
                VerificationMode.times(2));
        assertThat(cache.get()).isNull();
        loggerExtension.containsLog(
                Level.ERROR,
                "Failed to read %s".formatted(cacheConfiguration.getName()),
                "test",
                VerificationMode.times(3));
    }

    @Test
    void read_error_failOnCacheReadError() {

        cacheConfiguration.setFailOnCacheReadError(true);

        Cache<Object> cache = new Cache<>(cacheConfiguration, clockService) {
            @Override
            protected Object readData(Path activeVersion) {
                throw new RuntimeException("test");
            }

            @Override
            protected void writeData(Path target, Object data) {
                // Do nothing
            }
        };

        assertThat(catchThrowable(cache::read))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to read %s".formatted(cacheConfiguration.getName()));
    }

    @Test
    void loadDataOnStartup() throws IOException {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getActiveVersion().toPath());

        cacheConfiguration.setLoadDataOnStartup(true);

        Cache<Object> cache = new Cache<>(cacheConfiguration, clockService) {
            @Override
            protected Object readData(Path activeVersion) {
                return data;
            }

            @Override
            protected void writeData(Path target, Object data) {
                // Do nothing
            }
        };

        cache.loadDataOnStartup();

        assertThat(cache.get()).isEqualTo(data);
        loggerExtension.containsLog(
                Level.INFO,
                "Read testCache data from `%s` with size 0.00MB in 310 ms".formatted(cacheConfiguration.getActiveVersion()
                        .getAbsolutePath()));
    }

    @Test
    void loadDataOnStartup_notAllowedToLoadOnStartup() throws IOException {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getActiveVersion().toPath());

        cacheConfiguration.setLoadDataOnStartup(false);

        Cache<Object> cache = new Cache<>(cacheConfiguration, clockService) {
            private int counter;

            @Override
            protected Object readData(Path activeVersion) {
                if (counter++ == 0) {
                    return null;
                }
                return data;
            }

            @Override
            protected void writeData(Path target, Object data) {
                // Do nothing
            }
        };

        cache.loadDataOnStartup();

        assertThat(cache.get()).isNull();
    }

    @Test
    void get() throws IOException {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getActiveVersion().toPath());

        Cache<Object> cache = new Cache<>(cacheConfiguration, clockService) {
            private int counter;

            @Override
            protected Object readData(Path activeVersion) {
                if (counter++ == 0) {
                    return data;
                }
                return null;
            }

            @Override
            protected void writeData(Path target, Object data) {
                // Do nothing
            }
        };

        assertThat(cache.get()).isEqualTo(data);
        assertThat(cache.get()).isEqualTo(data);

        loggerExtension.containsLog(
                Level.INFO,
                "Read testCache data from `%s` with size 0.00MB in 310 ms".formatted(cacheConfiguration.getActiveVersion()
                        .getAbsolutePath()));
    }

    @Test
    void write() throws IOException {
        String timestamp1 = "2022-03-11T09:03:01.123-01:00";
        String timestamp2 = "2022-03-11T09:04:01.123-01:00";
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(timestamp1, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(timestamp2, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Cache<String> cache = new Cache<>(cacheConfiguration, clockService) {
            @Override
            protected String readData(Path activeVersion) {

                try {
                    return Files.readString(activeVersion.resolve("file1.txt"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void writeData(Path target, String data) throws IOException {

                Files.writeString(target.resolve("file1.txt"), data);
            }
        };

        assertThat(Files.exists(cacheConfiguration.getFolder())).isFalse();

        cache.write("testData1");

        loggerExtension.containsLog(
                Level.INFO,
                "Writing %s to location: %s".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp1).toRealPath().toAbsolutePath()));
        loggerExtension.containsLog(
                Level.INFO,
                "Written %s data to `%s` with size 0.00MB in 60000 ms".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp1).toRealPath().toAbsolutePath()));
        assertThat(cache.get()).isEqualTo("testData1");

        // verify we can load data from the disk
        cache.read();
        assertThat(cache.get()).isEqualTo("testData1");

        verifySymLink(timestamp1);

        // 2nd write
        String timestamp3 = "2022-03-11T09:03:02.123-01:00";
        String timestamp4 = "2022-03-11T09:04:02.123-01:00";
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(timestamp3, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(timestamp4, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        cache.write("testData2");

        loggerExtension.containsLog(
                Level.INFO,
                "Writing %s to location: %s".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp3).toRealPath().toAbsolutePath()));
        loggerExtension.containsLog(
                Level.INFO,
                "Written %s data to `%s` with size 0.00MB in 60000 ms".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp3).toRealPath().toAbsolutePath()));
        assertThat(cache.get()).isEqualTo("testData2");

        // verify we can load data from the disk
        cache.read();
        assertThat(cache.get()).isEqualTo("testData2");

        verifySymLink(timestamp3);
    }

    @Test
    void write_error() throws IOException {
        String timestamp1 = "2022-03-11T09:03:01.123-01:00";
        String timestamp2 = "2022-03-11T09:04:01.123-01:00";
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(timestamp1, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(timestamp2, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getActiveVersion().toPath());

        Cache<String> cache = new Cache<>(cacheConfiguration, clockService) {
            @Override
            protected String readData(Path activeVersion) {
                return null;
            }

            @Override
            protected void writeData(Path target, String data) throws IOException {
                throw new IOException("error");
            }
        };

        cache.write("testData1");
        assertThat(cache.get()).isNull();

        loggerExtension.containsLog(
                Level.ERROR,
                "Failed to write %s to file: %s".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp1).toRealPath().toAbsolutePath()));
    }

    @Test
    void getSizeInBytes() throws IOException {

        Cache<String> cache = new Cache<>(cacheConfiguration, clockService) {
            @Override
            protected String readData(Path activeVersion) {
                return null;
            }

            @Override
            protected void writeData(Path target, String data) {
                // to nothing
            }
        };

        Path file = testDir.resolve("file");
        Files.writeString(file, "6bytes");

        assertThat(cache.getSizeInBytes(file)).isEqualTo(6);
        assertThat(cache.getSizeInBytes(testDir)).isEqualTo(6);
    }

    private void verifySymLink(String timestamp) throws IOException {
        var activeVersion = cacheConfiguration.getActiveVersion().toPath();
        assertThat(activeVersion.toFile()).exists();

        assertThat(activeVersion.toRealPath().toAbsolutePath())
                .isEqualTo(cacheConfiguration.getFolder().resolve(timestamp).toRealPath().toAbsolutePath());
    }

    @Test
    void loadDataOnStartup_annotations() {
        AnnotationUtil.methodContainsAnnotation(
                Cache.class,
                EventListener.class,
                "loadDataOnStartup",
                eventListener -> assertThat(eventListener.value()).containsExactly(ApplicationStartedEvent.class));
    }
}
