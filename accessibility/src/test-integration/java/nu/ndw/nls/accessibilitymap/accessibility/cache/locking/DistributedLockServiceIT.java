package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.LockConfiguration;
import nu.ndw.nls.springboot.core.time.ClockBeanConfiguration;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@JooqTest
@Import({
        DistributedLockService.class,
        DistributedLockRepository.class,
        LockConfiguration.class,
        LockOwner.class,
        ClockBeanConfiguration.class
})

/*
  This test is not transactional because of multithreading issues on transactions running on the main thread, while the database
  actions on the executor threads will not be rolled back by the transaction manager, resulting in conflicts.
  This disables the transactional behaviour of the test class specified in the @JooqTest annotation.
  Explicit database clean-up is handled by the @AfterEach method.
  */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DistributedLockServiceIT {

    @Autowired
    private DistributedLockService lockService;

    @Autowired
    private DSLContext dsl;

    private static final String LOCK_NAME = "multi-thread-lock-test";

    @AfterEach
    void cleanup() {
        //noinspection SqlWithoutWhere
        dsl.execute("DELETE FROM accessibility_map.distributed_locks");
    }

    @Test
    void only_one_thread_can_acquire_lock() throws Exception {
        int threads = 5;
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(() -> {
                    try {
                        start.await();
                        return lockService.tryLock(LOCK_NAME);
                    } finally {
                        done.countDown();
                    }
                }));
            }
            start.countDown();
            done.await();
            int success = 0;
            for (Future<Boolean> f : results) {
                if (f.get()) {
                    success++;
                }
            }
            executor.shutdown();
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(success)
                    .as("Only one thread should acquire the lock")
                    .isEqualTo(1);
        }
    }

    @Test
    void lockOrFail_allows_only_one_thread_to_succeed() throws Exception {
        int threads = 5;
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            CountDownLatch startGate = new CountDownLatch(1);
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(() -> {
                    startGate.await();

                    try {
                        lockService.lockOrFail(LOCK_NAME, Duration.ofSeconds(2));
                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                }));
            }
            startGate.countDown();
            int success = 0;
            for (Future<Boolean> f : results) {
                if (f.get()) {
                    success++;
                }
            }
            executor.shutdown();
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(success)
                    .as("Only one thread should succeed in lockOrFail")
                    .isEqualTo(1);
        }
    }

    @Test
    void unlock_releases_lock_for_same_owner() {

        boolean acquired = lockService.tryLock(LOCK_NAME);

        assertThat(acquired).isTrue();

        lockService.unlock(LOCK_NAME);

        boolean reacquired = lockService.tryLock(LOCK_NAME);

        assertThat(reacquired).isTrue();
    }
}
