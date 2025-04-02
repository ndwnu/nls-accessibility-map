package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
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

    @Mock
    private FileSystem fileSystem;

    @Mock
    private WatchService watchService;


    private Path testDir;

    private Path workingDir;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());
        workingDir = testDir.resolve("testFolder");

        trafficSignCacheConfiguration = TrafficSignCacheConfiguration.builder()
                .folder(testDir.resolve("testFolder"))
                .fileNameActiveVersion("active")
                .build();

        trafficSignCacheUpdater = new TrafficSignCacheUpdater(trafficSignCacheConfiguration, trafficSignDataService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void watchFileChanges_fileChanges() throws InterruptedException, IOException {

        trafficSignCacheUpdater.watchFileChanges();
        Files.createFile(trafficSignCacheConfiguration.getActiveVersion().toPath());

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            loggerExtension.containsLog(Level.INFO, "Watching file changes in %s".formatted(workingDir));
            verify(trafficSignDataService, times(2)).updateTrafficSignData();
            loggerExtension.containsLog(Level.INFO, "Triggerd update");
        });

        assertThat(trafficSignCacheUpdater.fileWatcherThread.isInterrupted()).isFalse();
    }

    @Test
    void watchFileChanges_loadOnApplicationStart() throws InterruptedException, IOException {

        trafficSignCacheUpdater.watchFileChanges();
        verify(trafficSignDataService).updateTrafficSignData();

        assertThat(trafficSignCacheUpdater.fileWatcherThread.isInterrupted()).isFalse();
    }

    @Test
    void destroy() throws IOException {

        trafficSignCacheUpdater.watchFileChanges();
        trafficSignCacheUpdater.destroy();

        assertThat(trafficSignCacheUpdater.fileWatcherThread.isInterrupted()).isTrue();
    }

    @Test
    void destroy_verifyWatcherClosed() throws IOException {

        trafficSignCacheUpdater.watchService = watchService;
        trafficSignCacheUpdater.destroy();

        verify(watchService).close();
    }

    @Test
    void destroy_verifyWatcherClosed_handleIoExceptions() throws IOException {

        trafficSignCacheUpdater.watchService = watchService;

        doThrow(new IOException("some error")).when(watchService).close();
        trafficSignCacheUpdater.destroy();

        verify(watchService).close();
        loggerExtension.containsLog(Level.WARN, "Failed to stop watching file changes", "some error");
    }

    @Test
    void destroy_withoutInitialisation() {

        trafficSignCacheUpdater.destroy();

        assertThat(trafficSignCacheUpdater.watchService).isNull();
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