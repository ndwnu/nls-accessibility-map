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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;

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

    private RetryTemplate retryTemplate = RetryTemplate.builder()
            .maxAttempts(5)
            .fixedBackoff(1000)
            .build();

    private TYPE data;

    private String activeVersion;

    private int consecutiveReadFailures;

    @Getter(AccessLevel.PROTECTED)
    private final ReentrantLock dataLock = new ReentrantLock();

    private final ReentrantLock cacheSwitchLock = new ReentrantLock();

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

    public void read() {
        read(false);
    }

    protected synchronized void read(boolean triggeredOnStartup) {
        try {

            OffsetDateTime start = clockService.now();
            Path activeVersion = cacheConfiguration.getActiveVersion().toPath().toAbsolutePath().toRealPath();
            log.info("Reading {} from location: {}", cacheConfiguration.getName(), activeVersion.toAbsolutePath());

            TYPE newData = readData(activeVersion);
            setData(newData, activeVersion);

            log.info(
                    "Read {} data from `{}` with size {}MB in {} ms",
                    cacheConfiguration.getName(),
                    activeVersion,
                    BigDecimal.valueOf(getSizeInBytes(activeVersion))
                            .divide(BINARY_KILO.multiply(BINARY_KILO), SIZE_ROUNDING, RoundingMode.HALF_UP),
                    Duration.between(start, clockService.now()).toMillis());
            consecutiveReadFailures = 0;
        } catch (Exception exception) {
            consecutiveReadFailures += 1;
            if (consecutiveReadFailures > cacheConfiguration.getAcceptableConsequentReadFailures()) {
                log.error("Failed to read {}", cacheConfiguration.getName(), exception);
            }
            if (triggeredOnStartup && cacheConfiguration.isFailOnStartupCacheReadError()) {
                throw new IllegalStateException("Failed to read %s".formatted(cacheConfiguration.getName()), exception);
            }
        } finally {
            publishCacheLoadedEvent();
        }
    }

    protected void setData(TYPE data, Path activeVersion) {
        dataLock.lock();
        this.activeVersion = activeVersion.getFileName().toString();
        this.data = data;
        dataLock.unlock();
    }

    @SneakyThrows
    protected boolean isDataStale() {
        Path activeVersionOnDisk = cacheConfiguration.getActiveVersion().toPath().toAbsolutePath().toRealPath();
        String activeCurrent = activeVersionOnDisk.getFileName().toString();
        log.debug("Active version on disk: {}", activeCurrent);
        log.debug("Active version in cache: {}", this.activeVersion);
        return !activeCurrent.equals(this.activeVersion);
    }

    public void write(Supplier<TYPE> networkDataSupplier) {
        OffsetDateTime start = clockService.now();
        Path targetFolder = Path.of(start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        Path targetLocation = cacheConfiguration.getFolder().resolve(targetFolder);
        try {
            distributedLockService.lockOrFail(cacheConfiguration.getName(), getCacheConfiguration().getMaxLockWaitTime());
            TYPE newData = networkDataSupplier.get();
            Files.createDirectories(targetLocation);
            log.info("Writing {} to location: {}", cacheConfiguration.getName(), targetLocation.toFile().getAbsolutePath());
            writeData(targetLocation.toRealPath().toAbsolutePath(), newData);

            log.info(
                    "Written {} data to `{}` with size {}MB in {} ms",
                    cacheConfiguration.getName(),
                    targetLocation.toFile().getAbsolutePath(),
                    BigDecimal.valueOf(getSizeInBytes(targetLocation))
                            .divide(BINARY_KILO.multiply(BINARY_KILO), SIZE_ROUNDING, RoundingMode.HALF_UP),
                    Duration.between(start, clockService.now()).toMillis());

            switchSymLink(targetFolder);
            setData(newData, targetLocation);
        } catch (IOException exception) {
            log.error("Failed to write {} to file: {}", cacheConfiguration.getName(), targetLocation, exception);
        } finally {
            distributedLockService.unlock(cacheConfiguration.getName());
        }
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

    protected void switchSymLink(Path target) throws IOException {
        try {
            cacheSwitchLock.lock();
            retryTemplate.execute(context -> {
                try {
                    Path symlink = cacheConfiguration.getActiveVersion().toPath();
                    Path oldTarget = null;
                    if (Files.isSymbolicLink(symlink)) {
                        if (Files.exists(symlink)) {
                            oldTarget = symlink.toRealPath();
                        }
                        Files.delete(symlink);
                    }

                    Files.createSymbolicLink(symlink, target);
                    log.debug("Updated symlink: {}", cacheConfiguration.getActiveVersion().getAbsolutePath());
                    log.debug("Update symlink old {} new {}", oldTarget, target);
                    if (Objects.nonNull(oldTarget)) {
                        FileUtils.deleteDirectory(oldTarget.toFile());
                        log.debug("Removed old symlink target: {}", oldTarget.toAbsolutePath());
                    }
                } catch (IOException exception) {
                    log.debug("Failed to update symlink ", exception);
                    throw new IllegalStateException("Failed to update symlink", exception);
                }
                return null;
            });
        } finally {
            cacheSwitchLock.unlock();
        }
    }

    public boolean dataExists() {
        return Files.exists(getCacheConfiguration().getActiveVersion().toPath());
    }
}
