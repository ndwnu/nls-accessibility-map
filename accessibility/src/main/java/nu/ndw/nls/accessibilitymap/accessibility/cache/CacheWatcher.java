package nu.ndw.nls.accessibilitymap.accessibility.cache;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class CacheWatcher<TYPE> {

    @Getter(AccessLevel.PROTECTED)
    private final CacheConfiguration cacheConfiguration;

    @Getter(AccessLevel.PROTECTED)
    private final Cache<TYPE> cache;

    protected Thread fileWatcherThread;

    /**
     * When the application starts, it will start to watch for file changes of the current active traffic sign cache. Normally we would use
     * a File watcher, but as it turns out, that is not reliable on azure.
     */
    @EventListener(ApplicationStartedEvent.class)
    @SuppressWarnings({"java:S1166", "java:S2142", "java:S134"})
    public void watchFileChanges() throws IOException {
        if (!cacheConfiguration.isWatchForUpdates()) {
            return;
        }

        Files.createDirectories(cacheConfiguration.getFolder());

        fileWatcherThread = new Thread(() -> {

            long lastModified = cacheConfiguration.getActiveVersion().lastModified();
            log.info("Watching file changes on {}", cacheConfiguration.getActiveVersion());

            while (true) {
                try {
                    if (lastModified != cacheConfiguration.getActiveVersion().lastModified()) {
                        lastModified = cacheConfiguration.getActiveVersion().lastModified();

                        log.info("Triggering update");
                        cache.read();
                        log.info("Finished update");
                    }
                } finally {
                    try {
                        Thread.sleep(cacheConfiguration.getFileWatcherInterval().toMillis());
                    } catch (InterruptedException exception) {
                        log.error("Failed to sleep", exception);
                    }
                }
            }
        });
        fileWatcherThread.start();
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(fileWatcherThread)) {
            fileWatcherThread.interrupt();
        }
    }
}
