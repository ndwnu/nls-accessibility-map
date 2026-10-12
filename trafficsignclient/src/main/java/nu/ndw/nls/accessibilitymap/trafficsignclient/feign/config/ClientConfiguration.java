package nu.ndw.nls.accessibilitymap.trafficsignclient.feign.config;

import feign.Retryer;
import nu.ndw.nls.springboot.client.feign.configuration.NlsClientConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * In open api generator, set generateSupportingFiles property to false to allow supplying a custom implementation. Do
 * not turn this class into a bean to avoid leaking this configuration in the spring boot context to other feign
 * clients.
 */
public class ClientConfiguration extends NlsClientConfiguration {

    /**
     * Retrying is handled by resilience4j with exponential backoff, disabling the feign retryer prevents nested retries.
     */
    @Bean
    @Override
    public Retryer getRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
