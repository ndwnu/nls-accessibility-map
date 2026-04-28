package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.LockConfiguration;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DistributedLockServiceTest {

    private static final String LOCK_NAME = "lockName";

    @Mock
    private DistributedLockRepository repository;

    @Mock
    private LockConfiguration lockConfiguration;

    @Mock
    private ClockService clockService;

    @Mock
    private OffsetDateTime offsetDateTime;

    private DistributedLockService distributedLockService;

    @BeforeEach
    void setup() {
        distributedLockService = new DistributedLockService(repository, lockConfiguration, clockService);
    }

    @Test
    void tryLock() {
        Instant instant = Instant.parse("2026-04-01T12:30:00Z");
        when(clockService.now())
                .thenReturn(offsetDateTime);
        when(offsetDateTime.toInstant())
                .thenReturn(instant);
        when(lockConfiguration.getDefaultLockTtl()).thenReturn(Duration.ofSeconds(1));
        when(repository.tryAcquireLock(LOCK_NAME, instant, instant.plus(lockConfiguration.getDefaultLockTtl()))).thenReturn(true);

        assertThat(distributedLockService.tryLock(LOCK_NAME)).isTrue();
    }

    @Test
    void lockOrFail() {
        Instant instant = Instant.parse("2026-04-01T12:30:00Z");
        when(clockService.now())
                .thenReturn(offsetDateTime);
        when(offsetDateTime.toInstant())
                .thenReturn(instant, instant, instant, instant.plus(Duration.ofSeconds(2)));
        when(lockConfiguration.getDefaultLockTtl()).thenReturn(Duration.ofSeconds(1));
        when(repository.tryAcquireLock(LOCK_NAME, instant, instant.plus(lockConfiguration.getDefaultLockTtl())))
                .thenReturn(true);

        distributedLockService.lockOrFail(LOCK_NAME, Duration.ofSeconds(2));

        verify(repository).tryAcquireLock(LOCK_NAME, instant, instant.plus(lockConfiguration.getDefaultLockTtl()));
    }

    @Test
    void lockOrFail_will_wait_for_lock() {
        when(lockConfiguration.getLockRetryInterval()).thenReturn(Duration.ofMillis(100));
        Duration step = lockConfiguration.getLockRetryInterval();
        Instant start = Instant.parse("2026-04-01T12:30:00Z");
        AtomicInteger counter = new AtomicInteger();
        when(clockService.now()).thenAnswer(invocation -> {
            int i = counter.getAndIncrement();
            return start.atOffset(ZoneOffset.UTC).plus(step.multipliedBy(i));
        });
        when(lockConfiguration.getDefaultLockTtl()).thenReturn(Duration.ofSeconds(3));
        when(repository.tryAcquireLock(eq(LOCK_NAME), any(),
                any())).thenReturn(false, true);

        distributedLockService.lockOrFail(LOCK_NAME, Duration.ofSeconds(1));

        verify(repository, times(2)).tryAcquireLock(eq(LOCK_NAME), any(),
                any());
    }

    @Test
    void lockOrFail_will_timeout() {

        when(lockConfiguration.getLockRetryInterval()).thenReturn(Duration.ofMillis(500));
        Duration step = lockConfiguration.getLockRetryInterval();
        Instant start = Instant.parse("2026-04-01T12:30:00Z");
        AtomicInteger counter = new AtomicInteger();
        when(clockService.now()).thenAnswer(invocation -> {
            int i = counter.getAndIncrement();
            return start.atOffset(ZoneOffset.UTC).plus(step.multipliedBy(i));
        });

        when(lockConfiguration.getDefaultLockTtl()).thenReturn(Duration.ofSeconds(2));
        when(repository.tryAcquireLock(eq(LOCK_NAME),
                any(), any())).thenReturn(false);

        assertThatThrownBy(() -> distributedLockService.lockOrFail(LOCK_NAME, Duration.ofSeconds(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not acquire lock 'lockName' within 1 seconds");
    }

    @Test
    void unlock() {
        distributedLockService.unlock(LOCK_NAME);
        verify(repository).releaseLock(LOCK_NAME);
    }
}
