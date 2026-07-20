package nu.ndw.nls.accessibilitymap.accessibility.cache.configuration;

import jakarta.validation.constraints.NotNull;
import java.nio.file.DirectoryNotEmptyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryState;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class RetryConfiguration {

    private static final int DEFAULT_RETRY_COUNT = 10;

    @Bean
    public RetryTemplate directoryNotEmptyRetryTemplate() {

        RetryPolicy retryPolicy = RetryPolicy.builder()
                .backOff(new FixedBackOff(1000, DEFAULT_RETRY_COUNT))
                .includes(DirectoryNotEmptyException.class)
                .build();
        RetryTemplate retryTemplate = new RetryTemplate(retryPolicy);
        retryTemplate.setRetryListener(new RetryListener() {
            @SuppressWarnings("NullableProblems")
            @Override
            public void beforeRetry(
                    @NotNull RetryPolicy policy,
                    @NotNull Retryable<?> retryable,
                    @NotNull RetryState state
            ) {

                log.warn(
                        "Directory not empty, retrying (attempt {})",
                        state.getRetryCount());
            }
        });
        return retryTemplate;
    }
}
