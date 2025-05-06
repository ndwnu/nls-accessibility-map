package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "nu.ndw.nls.accessibilitymap.trafficsigns.cache.filewatcher.enabled",
        havingValue = "true", matchIfMissing = true
)
public class TrafficSignCacheUpdater {

    private final TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    private final TrafficSignDataService trafficSignDataService;

    private final GraphHopperService graphHopperService;

    protected Thread fileWatcherThread;

    /**
     * When the application starts it will start to watch for file changes of the current active traffic sign cache. Normally we would use a
     * File watcher but as it turns out that is not reliable on azure.
     */
    @EventListener(ApplicationStartedEvent.class)
    @SuppressWarnings({"java:S1166", "java:S2142", "java:S134"})
    public void watchFileChanges() throws IOException {
        Files.createDirectories(trafficSignCacheConfiguration.getFolder());

        fileWatcherThread = new Thread(() -> {

            long lastModified = trafficSignCacheConfiguration.getActiveVersion().lastModified();
            log.info("Watching file changes on {}", trafficSignCacheConfiguration.getActiveVersion());

            while (true) {
                try {
                    if (lastModified != trafficSignCacheConfiguration.getActiveVersion().lastModified()) {
                        lastModified = trafficSignCacheConfiguration.getActiveVersion().lastModified();

                        try {
                            updateCache(graphHopperService.getNetworkGraphHopper());
                        } catch (RuntimeException runtimeException) {
                            log.error("Failed to update traffic signs data", runtimeException);
                        }
                    }
                } finally {
                    try {
                        Thread.sleep(trafficSignCacheConfiguration.getFileWatcherInterval().toMillis());
                    } catch (InterruptedException exception) {
                        log.error("Failed to sleep", exception);
                    }
                }
            }
        });
        fileWatcherThread.start();
    }

    public void updateCache(NetworkGraphHopper networkGraphHopper) {

        log.info("Triggering update");
        trafficSignDataService.updateTrafficSignData(networkGraphHopper);
        log.info("Finished update");
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(fileWatcherThread)) {
            fileWatcherThread.interrupt();
        }
    }
}
