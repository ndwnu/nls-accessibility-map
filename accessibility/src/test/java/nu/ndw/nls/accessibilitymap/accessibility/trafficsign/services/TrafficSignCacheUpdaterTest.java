package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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
class TrafficSignCacheUpdaterTest {

    private TrafficSignCacheUpdater trafficSignCacheUpdater;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;
    @Mock
    private GraphHopperService graphHopperService;

    private TrafficSignCacheConfiguration trafficSignCacheConfiguration;

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

        trafficSignCacheUpdater = new TrafficSignCacheUpdater(trafficSignCacheConfiguration, trafficSignDataService, graphHopperService);
    }

    @AfterEach
    void tearDown() throws IOException {

        trafficSignCacheUpdater.destroy();
        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    @SuppressWarnings("java:S2925")
    void watchFileChanges_fileChanges() throws IOException {

        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        Files.createDirectories(trafficSignCacheConfiguration.getFolder());
        Files.createFile(trafficSignCacheConfiguration.getActiveVersion().toPath());
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> Files.exists(trafficSignCacheConfiguration.getActiveVersion().toPath()));

        trafficSignCacheUpdater.watchFileChanges();

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> loggerExtension.containsLog(Level.INFO,
                "Watching file changes on %s".formatted(trafficSignCacheConfiguration.getActiveVersion())));

        Files.writeString(trafficSignCacheConfiguration.getActiveVersion().toPath(), "changed");

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            loggerExtension.containsLog(Level.INFO, "Triggering update", VerificationMode.atLeastOnce());
            loggerExtension.containsLog(Level.INFO, "Finished update", VerificationMode.atLeastOnce());
            verify(trafficSignDataService, atLeastOnce()).updateTrafficSignData(networkGraphHopper);
        });

        assertThat(trafficSignCacheUpdater.fileWatcherThread.isInterrupted()).isFalse();
    }

    @Test
    @SuppressWarnings("java:S2925")
    void watchFileChanges_failedToUpdateData() throws IOException {

        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        doThrow(new RuntimeException("some error")).when(trafficSignDataService).updateTrafficSignData(networkGraphHopper);

        Files.createDirectories(trafficSignCacheConfiguration.getFolder());
        Files.createFile(trafficSignCacheConfiguration.getActiveVersion().toPath());
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> Files.exists(trafficSignCacheConfiguration.getActiveVersion().toPath()));

        trafficSignCacheUpdater.watchFileChanges();

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> loggerExtension.containsLog(Level.INFO,
                "Watching file changes on %s".formatted(trafficSignCacheConfiguration.getActiveVersion())));

        Files.writeString(trafficSignCacheConfiguration.getActiveVersion().toPath(), "changed");

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                loggerExtension.containsLog(
                        Level.ERROR,
                        "Failed to update traffic signs data",
                        "some error",
                        VerificationMode.atLeastOnce()));

        assertThat(trafficSignCacheUpdater.fileWatcherThread.isInterrupted()).isFalse();
    }

    @Test
    void updateCache() {

        trafficSignCacheUpdater.updateCache(networkGraphHopper);

        verify(trafficSignDataService).updateTrafficSignData(networkGraphHopper);

        loggerExtension.containsLog(Level.INFO, "Triggering update");
        loggerExtension.containsLog(Level.INFO, "Finished update");
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
