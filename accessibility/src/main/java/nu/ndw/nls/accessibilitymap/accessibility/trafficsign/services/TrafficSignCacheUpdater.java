package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
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

    private static final int ON_ERROR_RETRY_IN_MS = 1000;

    private final TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    private final TrafficSignDataService trafficSignDataService;

    protected WatchService watchService;

    protected Thread fileWatcherThread;

    @EventListener(ApplicationStartedEvent.class)
    @SuppressWarnings({"java:S1166", "java:S2142"})
    public void watchFileChanges() throws IOException {
        Files.createDirectories(trafficSignCacheConfiguration.getFolder());

        watchService = FileSystems.getDefault().newWatchService();
        trafficSignCacheConfiguration.getFolder().register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        fileWatcherThread = new Thread(() -> {

            try {
                log.info("Watching file changes in {}", trafficSignCacheConfiguration.getFolder().toAbsolutePath());
                WatchKey key;
                while (Objects.nonNull((key = watchService.take()))) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE && event.context().toString()
                                .equals(trafficSignCacheConfiguration.getFileNameActiveVersion())) {

                            try {
                                trafficSignDataService.updateTrafficSignData();
                                log.info("Triggerd update");
                            } catch (Exception exception) {
                                log.error("Failed to update traffic signs data", exception);
                            }
                        }
                    }

                    key.reset();
                }
            } catch (InterruptedException | ClosedWatchServiceException e) {
                // Nothing to do here because the watcher has already been closed or the thread has been stopped by the destroy method.
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
