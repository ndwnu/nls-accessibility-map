package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.Import;

@JooqTest
@Import({
        DistributedLockRepository.class,
        LockOwner.class
})
class DistributedLockRepositoryIT {

    @Autowired
    DSLContext dsl;

    @Autowired
    DistributedLockRepository repository;

    @Autowired
    LockOwner lockInstance;

    @AfterEach
    void cleanDb() {
        dsl.deleteFrom(DSL.table("accessibility_map.distributed_locks"))
                .execute();
    }

    @Test
    void shouldAcquireLock_whenNotExists() {
        Instant now = Instant.now();
        Instant expiry = now.plus(30, ChronoUnit.SECONDS);

        boolean result = repository.tryAcquireLock("lock-1", now, expiry);

        assertThat(result).isTrue();

        var record = dsl.fetchOne(
                "SELECT * FROM accessibility_map.distributed_locks WHERE lock_name = ?",
                "lock-1"
        );

        assertThat(record).isNotNull();
        assertThat(record.get("owner_id")).isEqualTo(lockInstance.getLockOwnerId());
    }

    @Test
    void shouldNotAcquireLock_whenNotExpired() {
        Instant now = Instant.now();
        Instant expiry = now.plus(30, ChronoUnit.SECONDS);

        repository.tryAcquireLock("lock-1", now, expiry);

        boolean secondTry = repository.tryAcquireLock("lock-1", now, expiry);

        assertThat(secondTry).isFalse();
    }

    @Test
    void shouldStealLock_whenExpired() {
        Instant now = Instant.now();

        Instant expired = now.minus(10, ChronoUnit.SECONDS);
        Instant newExpiry = now.plus(30, ChronoUnit.SECONDS);

        dsl.execute("""
                    INSERT INTO accessibility_map.distributed_locks(lock_name, owner_id, lock_expiry)
                    VALUES (?, ?, ?)
                """, "lock-1", "old-owner", expired);

        boolean result = repository.tryAcquireLock("lock-1", now, newExpiry);

        assertThat(result).isTrue();

        var record = dsl.fetchOne(
                "SELECT * FROM accessibility_map.distributed_locks WHERE lock_name = ?",
                "lock-1"
        );

        assertThat(record).isNotNull()
                .extracting(r -> r.get("owner_id"))
                .isEqualTo(lockInstance.getLockOwnerId());
    }

    @Test
    void shouldReleaseLock_whenOwned() {
        Instant now = Instant.now();
        Instant expiry = now.plus(30, ChronoUnit.SECONDS);

        repository.tryAcquireLock("lock-1", now, expiry);

        int deleted = repository.releaseLock("lock-1");

        assertThat(deleted).isEqualTo(1);
    }

    @Test
    void shouldNotReleaseLock_whenNotOwned() {
        Instant now = Instant.now();

        dsl.execute("""
                    INSERT INTO accessibility_map.distributed_locks(lock_name, owner_id, lock_expiry)
                    VALUES (?, ?, ?)
                """, "lock-1", "someone-else", now.plusSeconds(60));

        int deleted = repository.releaseLock("lock-1");

        assertThat(deleted).isZero();
    }
}
