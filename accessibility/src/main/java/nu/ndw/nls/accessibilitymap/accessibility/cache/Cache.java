package nu.ndw.nls.accessibilitymap.accessibility.cache;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.active.ActiveVersionRepository;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.cache.exception.ActiveVersionNotFoundException;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public abstract class Cache<TYPE> {

    private static final BigDecimal BINARY_KILO = BigDecimal.valueOf(1024);

    private static final int SIZE_ROUNDING = 2;

    @Getter(AccessLevel.PROTECTED)
    private final CacheConfiguration cacheConfiguration;

    @Getter(AccessLevel.PROTECTED)
    private final ClockService clockService;

    @Getter(AccessLevel.PROTECTED)
    private final DistributedLockService distributedLockService;

    @Getter(AccessLevel.PROTECTED)
    private final ActiveVersionRepository activeVersionRepository;

    private TYPE data;

    private int consecutiveReadFailures;

    @Getter(AccessLevel.PROTECTED)
    private final ReentrantLock dataLock = new ReentrantLock();

    private final RetryTemplate directoryNotEmptyRetryTemplate;

    private String activeVersion;

    @EventListener(ApplicationStartedEvent.class)
    public void loadDataOnStartup() {
        if (cacheConfiguration.isLoadDataOnStartup()) {
            this.read(true);
        }
    }

    public TYPE get() {
        if (Objects.isNull(data)) {
            read();
        }

        dataLock.lock();
        try {
            return data;
        } finally {
            dataLock.unlock();
        }
    }

    //    @Retryable(
//            retryFor = Exception.class,
//            maxAttempts = 10,
//            backoff = @Backoff(
//                    delay = 1000,
//                    multiplier = 2.0
//            )
//    )
    public void read() {
        read(false);
    }

    public boolean dataExists() {
        return getActiveVersionRepository()
                .findActiveVersion(cacheConfiguration.getName())
                .map(versionName -> Files.exists(getCacheConfiguration().getFolder().resolve(versionName)))
                .orElse(false);
    }

    @Transactional
    public void write(Supplier<TYPE> networkDataSupplier) {
        OffsetDateTime start = clockService.now();
        Path targetFolder = Path.of(start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        Path targetLocation = cacheConfiguration.getFolder().resolve(targetFolder);
        try {
            distributedLockService.lockOrFail(cacheConfiguration.getName(), getCacheConfiguration().getMaxLockWaitTime());
            TYPE newData = networkDataSupplier.get();
            Files.createDirectories(targetLocation);
            log.info("Writing {} to location: {}", cacheConfiguration.getName(), targetLocation.toFile().getAbsolutePath());
            writeData(targetLocation, newData);

            log.info(
                    "Written {} data to `{}` with size {}MB in {} ms",
                    cacheConfiguration.getName(),
                    targetLocation.toFile().getAbsolutePath(),
                    BigDecimal.valueOf(getSizeInBytes(targetLocation))
                            .divide(BINARY_KILO.multiply(BINARY_KILO), SIZE_ROUNDING, RoundingMode.HALF_UP),
                    Duration.between(start, clockService.now()).toMillis());

            switchActiveVersion(targetFolder);
            setData(newData);
        } catch (IOException exception) {
            log.error("Failed to write {} to file: {}", cacheConfiguration.getName(), targetLocation, exception);
        } finally {
            distributedLockService.unlock(cacheConfiguration.getName());
        }
    }

    public boolean isDataStale() {
        String currentActiveVersion = getCurrentActiveVersion();
        return Objects.nonNull(activeVersion) && !activeVersion.equals(currentActiveVersion);
    }

    protected String getCurrentActiveVersion() {
        return activeVersionRepository.findActiveVersion(cacheConfiguration.getName())
                .orElseThrow(() -> new ActiveVersionNotFoundException(cacheConfiguration.getName()));
    }

    protected synchronized void read(boolean triggeredOnStartup) {

        try {
            OffsetDateTime start = clockService.now();
            Path activeVersion = getActiveVersion();

            log.info("Reading {} from location: {}", cacheConfiguration.getName(), activeVersion.toAbsolutePath());
            TYPE newData = readData(activeVersion);
            setData(newData);
            log.info(
                    "Read {} data from `{}` with size {}MB in {} ms",
                    cacheConfiguration.getName(),
                    activeVersion,
                    BigDecimal.valueOf(getSizeInBytes(activeVersion))
                            .divide(BINARY_KILO.multiply(BINARY_KILO), SIZE_ROUNDING, RoundingMode.HALF_UP),
                    Duration.between(start, clockService.now()).toMillis());
            consecutiveReadFailures = 0;
            publishCacheLoadedEvent();
        } catch (Exception exception) {
            consecutiveReadFailures += 1;
            if (consecutiveReadFailures > cacheConfiguration.getAcceptableConsequentReadFailures()) {
                log.error("Failed to read {}", cacheConfiguration.getName(), exception);
            }
            if (triggeredOnStartup && cacheConfiguration.isFailOnStartupCacheReadError()) {
                throw new IllegalStateException("Failed to read %s".formatted(cacheConfiguration.getName()), exception);
            }
        }
    }

    protected Path getActiveVersion() {
        return activeVersionRepository.findActiveVersion(cacheConfiguration.getName())
                .map(activeVersionName -> cacheConfiguration.getFolder().resolve(activeVersionName))
                .orElseThrow(() -> new ActiveVersionNotFoundException(cacheConfiguration.getName()));
    }

    protected void setData(TYPE data) {
        dataLock.lock();
        this.data = data;
        this.activeVersion = getCurrentActiveVersion();
        dataLock.unlock();
    }

    protected abstract TYPE readData(Path activeVersion) throws IOException;

    protected abstract void writeData(Path target, TYPE data) throws IOException;

    protected abstract void publishCacheLoadedEvent();

    protected long getSizeInBytes(Path path) {
        if (Files.isDirectory(path)) {
            return FileUtils.sizeOfDirectory(path.toFile());
        } else {
            return path.toFile().length();
        }
    }

    protected void switchActiveVersion(Path target) throws IOException {
        Path oldVersionDirectory = activeVersionRepository.findActiveVersion(cacheConfiguration.getName())
                .map(oldActiveVersion -> cacheConfiguration.getFolder().resolve(oldActiveVersion).toFile().toPath())
                .orElse(null);

        activeVersionRepository.switchActiveVersion(cacheConfiguration.getName(), target.getFileName().toString());

        if (Objects.nonNull(oldVersionDirectory)) {
            try {
                directoryNotEmptyRetryTemplate.execute(context -> {
                    if (context.getRetryCount() > 0) {
                        log.warn("Directory not empty, retrying");
                    }
                    FileUtils.deleteDirectory(oldVersionDirectory.toFile());
                    return null;
                });
            } catch (Exception e) {
                if (e instanceof IOException ioException) {
                    throw ioException;
                }
                throw new IOException("Failed to delete old version directory: " + oldVersionDirectory, e);
            }
        }
    }
}
