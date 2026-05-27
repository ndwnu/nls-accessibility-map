package nu.ndw.nls.accessibilitymap.accessibility.cache;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import nu.ndw.nls.accessibilitymap.accessibility.cache.active.ActiveVersionRepository;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.logging.dto.VerificationMode;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
class CacheWatcherTest {

    private static final String CACHE_NAME = "testCache";

    private CacheWatcher<Object> cacheWatcher;

    private CacheConfiguration cacheConfiguration;

    @Mock
    private Cache<Object> cache;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    @Mock
    private ActiveVersionRepository activeVersionRepository;

    private Path testDir;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());

        cacheConfiguration = CacheConfiguration.builder()
                .name(CACHE_NAME)
                .folder(testDir.resolve("testFolder"))
                .fileWatcherInterval(Duration.ofMillis(1))
                .build();

        cacheWatcher = new CacheWatcher<>(cacheConfiguration, cache, taskScheduler, activeVersionRepository);
    }

    @AfterEach
    void tearDown() throws IOException {
        cacheWatcher.destroy();
        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void watchFileChanges_noActiveVersion_Exception() {
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cacheWatcher.watchFileChanges())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No active version found for cache %s".formatted(CACHE_NAME));
    }

    @Test
    void watchFileChanges_fileChanges() throws IOException {

        Path folder = cacheConfiguration.getFolder();
        Path activeFile = cacheConfiguration.getFolder().resolve("active");
        when(activeVersionRepository.findActiveVersion(CACHE_NAME)).thenReturn(Optional.of(activeFile.getFileName().toString()));

        Files.createDirectories(folder);
        Files.createFile(activeFile);

        AtomicReference<Runnable> capturedTask = new AtomicReference<>();

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            capturedTask.set(task);
            return scheduledFuture;
        }).when(taskScheduler)
                .scheduleWithFixedDelay(any(Runnable.class), eq(Duration.ofMillis(1)));

        cacheWatcher.watchFileChanges();

        loggerExtension.containsLog(
                Level.INFO,
                "Watching file changes on %s".formatted(activeFile)
        );

        capturedTask.get().run();

        Files.writeString(activeFile, "changed");

        Files.setLastModifiedTime(
                activeFile,
                FileTime.from(Instant.now().plusSeconds(2))
        );

        await()
                .atMost(2, SECONDS)
                .untilAsserted(() -> {
                    capturedTask.get().run();
                    loggerExtension.containsLog(Level.INFO, "Triggering update", VerificationMode.atLeastOnce());
                    loggerExtension.containsLog(Level.INFO, "Finished update", VerificationMode.atLeastOnce());
                    verify(cache, atLeast(1)).read();
                });
    }

    @Test
    void watchFileChanges_notWatchingForChanges() throws IOException {

        cacheConfiguration.setWatchForUpdates(false);

        cacheWatcher.watchFileChanges();

        verify(taskScheduler, org.mockito.Mockito.never())
                .scheduleWithFixedDelay(any(Runnable.class), eq(Duration.ofMillis(1)));
    }

    @Test
    void watchFileChanges_annotations() {
        AnnotationUtil.methodContainsAnnotation(
                CacheWatcher.class,
                EventListener.class,
                "watchFileChanges",
                eventListener -> assertThat(eventListener.value())
                        .containsExactly(ApplicationStartedEvent.class));
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
