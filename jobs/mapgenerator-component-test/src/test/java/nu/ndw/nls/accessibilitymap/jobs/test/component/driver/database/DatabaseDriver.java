package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database;

import static org.junit.Assert.fail;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import javax.sql.DataSource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.util.CompletableFuturesUtil;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseDriver implements StateManagement {

    private final DataSource dataSource;

    private final CompletableFuturesUtil completableFuturesUtil;

    private final DatabaseDriverConfiguration driverConfiguration;

    public void waitFor(final BooleanSupplier condition) {

        waitFor(condition, onErrorFailTest());
    }

    private final List<CrudRepository> repositories;

    @SuppressWarnings("java:S2925")
    // Thread.sleep is necessary here because otherwise it takes to long to shut down the thread.
    public void waitFor(final BooleanSupplier condition, @NonNull final BiConsumer<String, Exception> errorCallback) {

        final var workers = Executors.newFixedThreadPool(1);

        final var timeLimit = OffsetDateTime.now().plusSeconds(driverConfiguration.getAsyncTimeout().getSeconds());
        try {
            CompletableFuture.supplyAsync(() -> {
                while (OffsetDateTime.now().isBefore(timeLimit)) {
                    if (condition.getAsBoolean()) {
                        return null;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (final InterruptedException e) {
                        log.error("Failed to sleep for 10 ms.", e);
                    }
                }
                throw new RuntimeException("Timout reached");
            }, workers).get(driverConfiguration.getAsyncTimeout().getSeconds(), TimeUnit.SECONDS);
        } catch (final Exception e) {
			/*
				Shutting down the whole pool is the only way to really stop the thread for executing any further. This
				could be solved in different ways but because the same issue arose in the AbstractKafkaListener we have
				chosen the same solution, and forcing to stop all threads was the only way to guarantee the intended
				behaviour in AbstractKafkaListener.

				We do this because otherwise threads could be running forever if the condition is never reached causing
				unnecessary usage of resources.
			 */
            completableFuturesUtil.shutdownThreads(workers);

            if (e.getCause() instanceof AssertionError) {
                fail(e.getCause().getMessage());
            }
            final var errorMsg = String.format("Failed to get information within %s seconds",
                    driverConfiguration.getAsyncTimeout().getSeconds());
            errorCallback.accept(errorMsg, e);
        }
    }

    public BiConsumer<String, Exception> onErrorFailTest() {

        return (errorMessage, exception) -> {
            log.error(errorMessage, exception);
            fail(errorMessage);
        };
    }

    @Override
    public void prepareBeforeEachScenario() {
        StateManagement.super.prepareBeforeEachScenario();
    }

    @Override
    public void clearStateAfterEachScenario() {

        repositories.forEach(CrudRepository::deleteAll);
    }
}
