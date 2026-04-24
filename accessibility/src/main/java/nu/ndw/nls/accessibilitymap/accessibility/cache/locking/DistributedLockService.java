package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.LockConfiguration;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private final DistributedLockRepository repository;

    private final LockConfiguration lockConfiguration;

    private final ClockService clockService;

    /**
     * Try to acquire the lock immediately.
     *
     * @param lockName name of the lock
     * @return true if lock acquired, false otherwise
     */
    public boolean tryLock(String lockName) {
        Instant now = clockService.now().toInstant();
        Instant expiry = now.plus(lockConfiguration.getDefaultLockTtl());
        return repository.tryAcquireLock(lockName, now, expiry);
    }

    /**
     * Acquire the lock within a given timeout, else throw an exception.
     *
     * @param lockName name of the lock
     * @param timeout  maximum duration to wait
     */
    public void lockOrFail(String lockName, Duration timeout) {
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            OffsetDateTime startLock = clockService.now();
            CompletableFuture<Void> result = new CompletableFuture<>();
            Instant deadline = clockService.now().toInstant().plus(timeout);
            Runnable attempt = new Runnable() {
                @Override
                public void run() {
                    if (clockService.now().toInstant().isAfter(deadline)) {
                        result.completeExceptionally(new IllegalStateException(
                                "Could not acquire lock '" + lockName + "' within " + timeout.toSeconds() + " seconds"
                        ));
                    }
                    if (tryLock(lockName)) {
                        log.debug("Acquired lock '{}'", lockName);
                        result.complete(null);
                    } else {
                        scheduler.schedule(this,
                                lockConfiguration.getLockRetryInterval().toMillis(),
                                TimeUnit.MILLISECONDS);
                    }
                }
            };
            scheduler.execute(attempt);
            try {
                result.join();
                OffsetDateTime endLock = clockService.now();
                log.info("Acquiring a lock took {} ms", Duration.between(startLock, endLock).toMillis());
            } catch (CompletionException e) {
                throw mapException(e);
            }
        }
    }

    /**
     * Release the lock if held by this instance.
     *
     * @param lockName name of the lock
     */
    public void unlock(String lockName) {
        repository.releaseLock(lockName);
    }

    private RuntimeException mapException(CompletionException e) {
        Throwable cause = e.getCause();

        if (cause instanceof IllegalStateException exception) {
            return exception;
        }

        return new IllegalStateException("Unexpected error while acquiring lock", cause);
    }
}
