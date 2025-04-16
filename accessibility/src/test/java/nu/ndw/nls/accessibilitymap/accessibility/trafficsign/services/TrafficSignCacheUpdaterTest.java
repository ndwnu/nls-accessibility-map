package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
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
class TrafficSignCacheUpdaterTest {

    private TrafficSignCacheUpdater trafficSignCacheUpdater;

    private TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    private Path testDir;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());

        trafficSignCacheConfiguration = TrafficSignCacheConfiguration.builder()
                .folder(testDir.resolve("testFolder"))
                .fileNameActiveVersion("active")
                .fileWatcherInterval(Duration.ofMillis(1))
                .build();

        trafficSignCacheUpdater = new TrafficSignCacheUpdater(trafficSignCacheConfiguration, trafficSignDataService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    @SuppressWarnings("java:S2925")
    void watchFileChanges_fileChanges() throws IOException, InterruptedException {

        Files.createDirectories(trafficSignCacheConfiguration.getFolder());
        Files.createFile(trafficSignCacheConfiguration.getActiveVersion().toPath());
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> Files.exists(trafficSignCacheConfiguration.getActiveVersion().toPath()));

        trafficSignCacheUpdater.watchFileChanges();
        Thread.sleep(trafficSignCacheConfiguration.getFileWatcherInterval().toMillis() + 1);
        Files.writeString(trafficSignCacheConfiguration.getActiveVersion().toPath(), "changed");

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            loggerExtension.containsLog(Level.INFO,
                    "Watching file changes on %s".formatted(trafficSignCacheConfiguration.getActiveVersion()));
            verify(trafficSignDataService).updateTrafficSignData();
            loggerExtension.containsLog(Level.INFO, "Triggering update");
            loggerExtension.containsLog(Level.INFO, "Finished update");
        });

        assertThat(trafficSignCacheUpdater.fileWatcherThread.isInterrupted()).isFalse();
    }

    @Test
    @SuppressWarnings("java:S2925")
    void watchFileChanges_failedToUpdateData() throws IOException, InterruptedException {

        doThrow(new RuntimeException("some error")).when(trafficSignDataService).updateTrafficSignData();

        Files.createDirectories(trafficSignCacheConfiguration.getFolder());
        Files.createFile(trafficSignCacheConfiguration.getActiveVersion().toPath());
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> Files.exists(trafficSignCacheConfiguration.getActiveVersion().toPath()));

        trafficSignCacheUpdater.watchFileChanges();
        Thread.sleep(trafficSignCacheConfiguration.getFileWatcherInterval().toMillis() + 1);
        Files.writeString(trafficSignCacheConfiguration.getActiveVersion().toPath(), "changed");

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                loggerExtension.containsLog(Level.ERROR, "Failed to update traffic signs data", "some error"));

        assertThat(trafficSignCacheUpdater.fileWatcherThread.isInterrupted()).isFalse();
    }

    @Test
    void destroy() throws IOException {

        trafficSignCacheUpdater.watchFileChanges();
        trafficSignCacheUpdater.destroy();

        assertThat(trafficSignCacheUpdater.fileWatcherThread.isInterrupted()).isTrue();
    }

    @Test
    void destroy_withoutInitialisation() {

        trafficSignCacheUpdater.destroy();

        assertThat(trafficSignCacheUpdater.fileWatcherThread).isNull();
    }

    @Test
    void watchFileChanges_annotations() {
        AnnotationUtil.methodContainsAnnotation(
                trafficSignCacheUpdater.getClass(),
                EventListener.class,
                "watchFileChanges",
                eventListener -> assertThat(eventListener.value()).containsExactly(ApplicationStartedEvent.class));
    }

    @Test
    void destroy_annotations() {
        AnnotationUtil.methodContainsAnnotation(
                trafficSignCacheUpdater.getClass(),
                PreDestroy.class,
                "destroy",
                preDestroy -> assertThat(preDestroy).isNotNull());
    }
}