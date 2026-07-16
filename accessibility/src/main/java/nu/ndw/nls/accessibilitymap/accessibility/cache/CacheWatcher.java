package nu.ndw.nls.accessibilitymap.accessibility.cache;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;

@Slf4j
@RequiredArgsConstructor
public class CacheWatcher<TYPE> {

    @Getter(AccessLevel.PROTECTED)
    private final CacheConfiguration cacheConfiguration;

    @Getter(AccessLevel.PROTECTED)
    private final Cache<TYPE> cache;

    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledTask;

    @EventListener(ApplicationStartedEvent.class)
    public void watchFileChanges() throws IOException {
        if (!cacheConfiguration.isWatchForUpdates()) {
            return;
        }

        Files.createDirectories(cacheConfiguration.getFolder());
        log.info("Watching file changes on {}", cacheConfiguration.getName());

        scheduledTask = taskScheduler.scheduleWithFixedDelay(
                () -> {

                    if (cache.isDataStale()) {
                        log.info("Triggering update");
                        cache.read();
                        log.info("Finished update");
                    }
                }
                , cacheConfiguration.getFileWatcherInterval());
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(scheduledTask)) {
            log.info("Stopping file watcher");
            scheduledTask.cancel(true);
        }
    }
}
