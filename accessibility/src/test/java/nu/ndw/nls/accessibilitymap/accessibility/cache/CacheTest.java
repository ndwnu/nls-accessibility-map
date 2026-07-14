package nu.ndw.nls.accessibilitymap.accessibility.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.cache.active.ActiveVersionRepository;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.logging.dto.VerificationMode;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;

@ExtendWith(MockitoExtension.class)
class CacheTest {

    private static final Duration MAX_LOCK_WAIT_TIME = Duration.ofSeconds(10);

    private static final String CACHE_NAME = "testCache";

    private static final String ACTIVE_VERSION = "active";

    @Mock
    private ClockService clockService;

    @Mock
    private DistributedLockService distributedLockService;

    @Mock
    ActiveVersionRepository activeVersionRepository;

    @Mock
    private RetryTemplate retryTemplate;

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
                .name(CACHE_NAME)
                .folder(testDir.resolve("testFolder"))
                .maxLockWaitTime(MAX_LOCK_WAIT_TIME)
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(testDir.toFile());
    }

    @SneakyThrows
    @Test
    void read() {
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        Cache<Object> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
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

            @Override
            protected void afterCacheLoaded() {
                // not implemented

            }
        };

        assertThat(cache.get()).isNull();

        cache.read();

        assertThat(cache.get()).isEqualTo(data);

        loggerExtension.containsLog(
                Level.INFO,
                "Reading %s from location: %s".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(ACTIVE_VERSION).toAbsolutePath()),
                VerificationMode.times(2));
        loggerExtension.containsLog(
                Level.INFO,
                "Read testCache data from `%s` with size 0.00MB in 310 ms".formatted(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION)
                        .toAbsolutePath()),
                VerificationMode.times(2));
    }

    @Test
    void read_error() throws IOException {
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        cacheConfiguration.setAcceptableConsequentReadFailures(1);
        cacheConfiguration.setFailOnStartupCacheReadError(false);

        Cache<Object> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
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
    void read_error_failOnCacheReadError() throws IOException {
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        cacheConfiguration.setFailOnStartupCacheReadError(false);

        final Cache<Object> cache = getCache();
        assertThat(cache.get()).isNull();
        loggerExtension.containsLog(
                Level.ERROR,
                "Failed to read %s".formatted(cacheConfiguration.getName()),
                "test");
    }

    private Cache<Object> getCache() {
        Cache<Object> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
            @Override
            protected Object readData(Path activeVersion) {
                throw new RuntimeException("test");
            }

            @Override
            protected void writeData(Path target, Object data) {
                // Do nothing
            }
        };

        cache.read(true);
        return cache;
    }

    @Test
    void read_error_failOnCacheReadError_notTriggeredByStartup() throws IOException {
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        cacheConfiguration.setFailOnStartupCacheReadError(true);

        Cache<Object> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
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
    }

    @Test
    void loadDataOnStartup() throws IOException {
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        cacheConfiguration.setLoadDataOnStartup(true);

        final Cache<Object> cache = getObjectCache1();

        assertThat(cache.get()).isEqualTo(data);
        loggerExtension.containsLog(
                Level.INFO,
                "Read testCache data from `%s` with size 0.00MB in 310 ms".formatted(cacheConfiguration.getFolder()
                        .resolve(ACTIVE_VERSION)));
    }

    private Cache<Object> getObjectCache1() {
        Cache<Object> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
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
        return cache;
    }

    @Test
    void loadDataOnStartup_notAllowedToLoadOnStartup() throws IOException {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        cacheConfiguration.setFailOnStartupCacheReadError(true);
        cacheConfiguration.setLoadDataOnStartup(false);

        Cache<Object> cache = getObjectCache();

        assertThat(cache.get()).isNull();
    }

    private Cache<Object> getObjectCache() {
        Cache<Object> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
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
        return cache;
    }

    @Test
    void isDataStale_false() {
        String timestamp1 = "2022-03-11T09:03:01.123-01:00";
        when(activeVersionRepository.findActiveVersion(CACHE_NAME))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(timestamp1));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(timestamp1, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Cache<String> cache = createWriteCache();

        assertThat(Files.exists(cacheConfiguration.getFolder())).isFalse();

        cache.write(() -> "testData1");

        assertThat(cache.isDataStale()).isFalse();
    }

    @Test
    void isDataStale_nullFalse() {
        String timestamp1 = "2022-03-11T09:03:01.123-01:00";
        when(activeVersionRepository.findActiveVersion(CACHE_NAME))
                .thenReturn(Optional.of(timestamp1));

        Cache<String> cache = createWriteCache();

        assertThat(cache.isDataStale()).isFalse();
    }

    @Test
    void isDataStale_true() {
        String timestamp1 = "2022-03-11T09:03:01.123-01:00";
        String timestamp2 = "2022-03-11T09:04:01.123-01:00";
        when(activeVersionRepository.findActiveVersion(CACHE_NAME))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(timestamp1))
                .thenReturn(Optional.of(timestamp2));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(timestamp1, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Cache<String> cache = createWriteCache();

        assertThat(Files.exists(cacheConfiguration.getFolder())).isFalse();

        cache.write(() -> "testData1");

        assertThat(cache.isDataStale()).isTrue();
    }

    @Test
    void loadDataOnStartup_error() throws IOException {
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        cacheConfiguration.setFailOnStartupCacheReadError(true);
        cacheConfiguration.setLoadDataOnStartup(true);

        Cache<Object> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
            @Override
            protected Object readData(Path activeVersion) {
                throw new RuntimeException("test");
            }

            @Override
            protected void writeData(Path target, Object data) {
                // Do nothing
            }
        };

        assertThat(catchThrowable(cache::loadDataOnStartup))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to read %s".formatted(cacheConfiguration.getName()))
                .hasRootCauseMessage("test");
        assertThat(cache.get()).isNull();
    }

    @Test
    void get() throws IOException {
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        Cache<Object> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
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
                "Read testCache data from `%s` with size 0.00MB in 310 ms".formatted(cacheConfiguration.getFolder()
                        .resolve(ACTIVE_VERSION)));
    }

    @SneakyThrows
    @Test
    void write() {

        when(retryTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    Retryable<Object> retryable = invocation.getArgument(0);
                    return retryable.execute();
                });

        String timestamp1 = "2022-03-11T09:03:01.123-01:00";
        String timestamp2 = "2022-03-11T09:04:01.123-01:00";
        when(activeVersionRepository.findActiveVersion(CACHE_NAME))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(timestamp1));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(timestamp1, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(timestamp2, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Cache<String> cache = createWriteCache();

        assertThat(Files.exists(cacheConfiguration.getFolder())).isFalse();

        cache.write(() -> "testData1");

        loggerExtension.containsLog(
                Level.INFO,
                "Writing %s to location: %s".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp1).toAbsolutePath()));
        loggerExtension.containsLog(
                Level.INFO,
                "Written %s data to `%s` with size 0.00MB in 60000 ms".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp1).toAbsolutePath()));
        assertThat(cache.get()).isEqualTo("testData1");

        // verify we can load data from the disk
        cache.read();
        assertThat(cache.get()).isEqualTo("testData1");

        verifyVersion(timestamp1);

        // 2nd write
        String timestamp3 = "2022-03-11T09:03:02.123-01:00";
        String timestamp4 = "2022-03-11T09:04:02.123-01:00";
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(timestamp3, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(timestamp4, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        cache.write(() -> "testData2");

        verify(distributedLockService, times(2)).lockOrFail(cacheConfiguration.getName(), MAX_LOCK_WAIT_TIME);
        verify(distributedLockService, times(2)).unlock(cacheConfiguration.getName());

        loggerExtension.containsLog(
                Level.INFO,
                "Writing %s to location: %s".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp3).toAbsolutePath()));
        loggerExtension.containsLog(
                Level.INFO,
                "Written %s data to `%s` with size 0.00MB in 60000 ms".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp3).toAbsolutePath()));

        assertThat(cache.get()).isEqualTo("testData2");

        // verify we can load data from the disk
        cache.read();
        assertThat(cache.get()).isEqualTo("testData2");
        verifyVersion(timestamp3);
    }

    @NotNull
    private Cache<String> createWriteCache() {
        return new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
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
    }

    @Test
    void write_error() throws IOException {
        String timestamp1 = "2022-03-11T09:03:01.123-01:00";
        String timestamp2 = "2022-03-11T09:04:01.123-01:00";
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(timestamp1, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(timestamp2, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Files.createDirectories(cacheConfiguration.getFolder().resolve(ACTIVE_VERSION));

        Cache<String> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
            @Override
            protected String readData(Path activeVersion) {
                return null;
            }

            @Override
            protected void writeData(Path target, String data) throws IOException {
                throw new IOException("error");
            }
        };

        cache.write(() -> "testData1");
        assertThat(cache.get()).isNull();

        loggerExtension.containsLog(
                Level.ERROR,
                "Failed to write %s to file: %s".formatted(
                        cacheConfiguration.getName(),
                        cacheConfiguration.getFolder().resolve(timestamp1).toAbsolutePath()));
    }

    @Test
    void getSizeInBytes() throws IOException {

        Cache<String> cache = new Cache<>(
                cacheConfiguration,
                clockService,
                distributedLockService,
                activeVersionRepository,
                retryTemplate) {
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

    @SneakyThrows
    @Test
    void switchActiveVersion_retryException() {
        Path activeVersion = cacheConfiguration.getFolder().resolve(ACTIVE_VERSION);
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(ACTIVE_VERSION));
        when(retryTemplate.execute(any())).thenThrow(new RetryException("test", new RuntimeException("test")));
        assertThatThrownBy(() -> createWriteCache().switchActiveVersion(activeVersion))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to delete old version directory:");
    }

    private void verifyVersion(String timestamp) {
        var activeVersion = cacheConfiguration.getFolder().resolve(timestamp);
        assertThat(activeVersion.toFile()).exists();

        assertThat(activeVersion.toAbsolutePath())
                .isEqualTo(cacheConfiguration.getFolder().resolve(timestamp).toAbsolutePath());
        verify(activeVersionRepository).switchActiveVersion(CACHE_NAME, timestamp);
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
