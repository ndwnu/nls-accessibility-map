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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@RequiredArgsConstructor
@Slf4j
public abstract class Cache<TYPE> {

    private static final BigDecimal BINARY_KILO = BigDecimal.valueOf(1024);

    private static final int SIZE_ROUNDING = 2;

    @Getter(AccessLevel.PROTECTED)
    private final CacheConfiguration cacheConfiguration;

    @Getter(AccessLevel.PROTECTED)
    private final ClockService clockService;

    private TYPE data;

    private int consecutiveReadFailures;

    private final ReentrantLock dataLock = new ReentrantLock();

    @EventListener(ApplicationStartedEvent.class)
    public void loadDataOnStartup() {
        if (cacheConfiguration.isLoadDataOnStartup()) {
            this.read();
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

    public synchronized void read() {
        try {
            OffsetDateTime start = clockService.now();
            Path activeVersion = cacheConfiguration.getActiveVersion().toPath().toAbsolutePath().toRealPath();

            log.info("Reading {} from location: {}", cacheConfiguration.getName(), activeVersion.toAbsolutePath());
            TYPE newData = readData(activeVersion);

            dataLock.lock();
            this.data = newData;
            dataLock.unlock();

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
            if (cacheConfiguration.isFailOnCacheReadError()) {
                throw new IllegalStateException("Failed to read %s".formatted(cacheConfiguration.getName()), exception);
            }
        }
    }

    public void write(TYPE data) {
        OffsetDateTime start = clockService.now();
        Path targetFolder = Path.of(start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        Path targetLocation = cacheConfiguration.getFolder().resolve(targetFolder);
        try {
            Files.createDirectories(targetLocation);

            log.info("Writing {} to location: {}", cacheConfiguration.getName(), targetLocation.toFile().getAbsolutePath());
            writeData(targetLocation.toRealPath().toAbsolutePath(), data);
            log.info(
                    "Written {} data to `{}` with size {}MB in {} ms",
                    cacheConfiguration.getName(),
                    targetLocation.toFile().getAbsolutePath(),
                    BigDecimal.valueOf(getSizeInBytes(targetLocation))
                            .divide(BINARY_KILO.multiply(BINARY_KILO), SIZE_ROUNDING, RoundingMode.HALF_UP),
                    Duration.between(start, clockService.now()).toMillis());

            dataLock.lock();
            switchSymLink(targetFolder);
            this.data = data;
            dataLock.unlock();
        } catch (IOException exception) {
            log.error("Failed to write {} to file: {}", cacheConfiguration.getName(), targetLocation, exception);
        }
    }

    protected abstract TYPE readData(Path activeVersion) throws IOException;

    protected abstract void writeData(Path target, TYPE data) throws IOException;

    protected long getSizeInBytes(Path path) {
        if (Files.isDirectory(path)) {
            return FileUtils.sizeOfDirectory(path.toFile());
        } else {
            return path.toFile().length();
        }
    }

    private void switchSymLink(Path target) throws IOException {

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

        if (Objects.nonNull(oldTarget)) {
            FileUtils.deleteDirectory(oldTarget.toFile());
            log.debug("Removed old symlink target: {}", oldTarget.toAbsolutePath());
        }
    }
}
