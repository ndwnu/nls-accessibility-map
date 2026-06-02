package nu.ndw.nls.accessibilitymap.accessibility.cache.configuration;

import java.nio.file.DirectoryNotEmptyException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfiguration {

    private static final int DEFAULT_RETRY_COUNT = 10;

    @Bean
    public RetryTemplate directoryNotEmptyRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                DEFAULT_RETRY_COUNT,
                Map.of(DirectoryNotEmptyException.class, true)
        );

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1_000);

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
