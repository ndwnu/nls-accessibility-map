package nu.ndw.nls.accessibilitymap.jobs.test.component.core.util;

import static org.junit.Assert.fail;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.configuration.ConcurrentConfiguration;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompletableFuturesUtil {

	private final ConcurrentConfiguration configuration;

	public void shutdownThreads(final ExecutorService executorService) {

		try {
			executorService.shutdownNow();

			final var terminationSucceeded = executorService.awaitTermination(configuration.getThreadTerminationTimeout().getSeconds(), TimeUnit.SECONDS);
			if (!terminationSucceeded) {
				logError(null);
			}
		} catch (final InterruptedException terminationException) {
			logError(terminationException);
		}
	}

	private void logError(final Exception exception) {

		final var errorMsg = String.format("Failed stop remaining threads within %s seconds", configuration.getThreadTerminationTimeout().getSeconds());
		if (Objects.nonNull(exception)) {
			log.error(errorMsg, exception);
		} else {
			log.error(errorMsg);
		}

		fail(errorMsg);
	}
}
