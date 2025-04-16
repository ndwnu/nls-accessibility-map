package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrafficSignCacheUpdater {

    private final TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    private final TrafficSignDataService trafficSignDataService;

    protected WatchService watchService;

    protected Thread fileWatcherThread;

    @EventListener(ApplicationStartedEvent.class)
    @SuppressWarnings({"java:S1166", "java:S2142"})
    public void watchFileChanges() throws IOException {
        Files.createDirectories(trafficSignCacheConfiguration.getFolder());

        watchService = FileSystems.getDefault().newWatchService();
        trafficSignCacheConfiguration.getFolder().register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.OVERFLOW);

        fileWatcherThread = new Thread(() -> {

            log.info("Watching file changes in {}", trafficSignCacheConfiguration.getFolder().toAbsolutePath());
            long lastModified = trafficSignCacheConfiguration.getActiveVersion().lastModified();
            while (true) {

                try {
                    if (lastModified != trafficSignCacheConfiguration.getActiveVersion().lastModified()) {
                        lastModified = trafficSignCacheConfiguration.getActiveVersion().lastModified();
                        log.info("Triggering update");
                        trafficSignDataService.updateTrafficSignData();
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    log.info("File watcher thread interrupted");
                    return;
                }
            }
        });
        fileWatcherThread.start();
    }

    @PreDestroy
    public void destroy() {
        try {
            if (Objects.nonNull(watchService)) {
                watchService.close();
            }
            if (Objects.nonNull(fileWatcherThread)) {
                fileWatcherThread.interrupt();
            }
        } catch (IOException ioException) {
            log.warn("Failed to stop watching file changes", ioException);
        }
    }
}
