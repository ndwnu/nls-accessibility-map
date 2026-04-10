package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class DistributedLockService {

    private static final Duration DEFAULT_LOCK_TTL = Duration.ofSeconds(30);

    private static final Duration LOCK_RETRY_INTERVAL = Duration.ofMillis(100);

    private final DistributedLockRepository repository;

    private final Duration lockTtl;

    public DistributedLockService(DistributedLockRepository repository) {
        this.repository = repository;
        this.lockTtl = DEFAULT_LOCK_TTL;
    }

    /**
     * Try to acquire the lock immediately.
     *
     * @param lockName name of the lock
     * @return true if lock acquired, false otherwise
     */
    public boolean tryLock(String lockName) {
        Instant now = Instant.now();
        Instant expiry = now.plus(lockTtl);
        return repository.tryAcquireLock(lockName, now, expiry);
    }

    /**
     * Acquire the lock, retrying until it succeeds.
     *
     * @param lockName name of the lock
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void lock(String lockName) throws InterruptedException {
        while (!tryLock(lockName)) {
            Thread.sleep(LOCK_RETRY_INTERVAL.toMillis());
        }
    }

    /**
     * Acquire the lock within a given timeout, else throw exception.
     *
     * @param lockName name of the lock
     * @param timeout  maximum duration to wait
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public void lockOrFail(String lockName, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            if (tryLock(lockName)) {
                return;
            }
            Thread.sleep(LOCK_RETRY_INTERVAL.toMillis());
        }
        throw new IllegalStateException(
                "Could not acquire lock '" + lockName + "' within " + timeout.toSeconds() + " seconds"
        );
    }

    /**
     * Release the lock if held by this instance.
     *
     * @param lockName name of the lock
     */
    public void unlock(String lockName) {
        repository.releaseLock(lockName);
    }
}
