package nu.ndw.nls.accessibilitymap.job.speedlimits.client.configuration;

import feign.Retryer;
import nu.ndw.nls.roadattributesapi.client.feign.config.ClientConfiguration;
import org.springframework.context.annotation.Bean;

public class SpeedLimitsFeignClientConfiguration extends ClientConfiguration {

    /**
     * Retrying is handled by resilience4j with exponential backoff, disabling the feign retryer prevents nested retries.
     */
    @Bean
    @Override
    public Retryer getRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
