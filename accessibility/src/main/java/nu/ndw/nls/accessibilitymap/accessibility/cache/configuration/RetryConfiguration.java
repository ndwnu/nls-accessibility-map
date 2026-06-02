package nu.ndw.nls.accessibilitymap.accessibility.cache.configuration;

import java.nio.file.DirectoryNotEmptyException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfiguration {

    @Bean
    public RetryTemplate directoryNotEmptyRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                10,
                Map.of(DirectoryNotEmptyException.class, true)
        );

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1_000);

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    @Bean
    public RetryTemplate cacheReadRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1_000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(30_000);

        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                10,
                Map.of(
                        Exception.class, true
                ),
                true
        );

        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
