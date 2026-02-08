package nu.ndw.nls.accessibilitymap.accessibility.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.logging.dto.VerificationMode;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
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
class CacheWatcherTest {

    private CacheWatcher<Object> cacheWatcher;

    private CacheConfiguration cacheConfiguration;

    @Mock
    private Cache<Object> cache;

    private Path testDir;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());

        cacheConfiguration = CacheConfiguration.builder()
                .name("testCache")
                .folder(testDir.resolve("testFolder"))
                .fileNameActiveVersion("active")
                .fileWatcherInterval(Duration.ofMillis(1))
                .build();

        cacheWatcher = new CacheWatcher<>(cacheConfiguration, cache) {

        };
    }

    @AfterEach
    void tearDown() throws IOException {
        cacheWatcher.destroy();
        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    @SuppressWarnings("java:S2925")
    void watchFileChanges_fileChanges() throws IOException {
        Files.createDirectories(cacheConfiguration.getFolder());
        Files.createFile(cacheConfiguration.getActiveVersion().toPath());
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> Files.exists(cacheConfiguration.getActiveVersion().toPath()));

        cacheWatcher.watchFileChanges();

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> loggerExtension.containsLog(
                Level.INFO,
                "Watching file changes on %s".formatted(cacheConfiguration.getActiveVersion())));

        Files.writeString(cacheConfiguration.getActiveVersion().toPath(), "changed");

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            loggerExtension.containsLog(Level.INFO, "Triggering update", VerificationMode.atLeastOnce());
            loggerExtension.containsLog(Level.INFO, "Finished update", VerificationMode.atLeastOnce());
            verify(cache, times(2)).read();
        });

        assertThat(cacheWatcher.fileWatcherThread.isInterrupted()).isFalse();
    }

    @Test
    @SuppressWarnings("java:S2925")
    void watchFileChanges_notWatchingForChanges() throws IOException {

        cacheConfiguration.setWatchForUpdates(false);

        cacheWatcher.watchFileChanges();

        assertThat(cacheWatcher.fileWatcherThread).isNull();
    }

    @Test
    void watchFileChanges_annotations() {
        AnnotationUtil.methodContainsAnnotation(
                CacheWatcher.class,
                EventListener.class,
                "watchFileChanges",
                eventListener -> assertThat(eventListener.value()).containsExactly(ApplicationStartedEvent.class));
    }

    @Test
    void destroy_annotations() {
        AnnotationUtil.methodContainsAnnotation(
                CacheWatcher.class,
                PreDestroy.class,
                "destroy",
                preDestroy -> assertThat(preDestroy).isNotNull());
    }
}
