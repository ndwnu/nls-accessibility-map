package nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties;

import java.util.List;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;

class StreamConsumerExponentialBackoffPropertiesTest extends ValidationTest {

    @Test
    void exponentialBackoffConfig_nok_invalidValues() {
        StreamConsumerExponentialBackoffProperties config = StreamConsumerExponentialBackoffProperties.builder()
                .build();

        validate(
                config,
                List.of("initialIntervalInSeconds", "multiplier", "maxIntervalInSeconds"),
                List.of("must be greater than 0", "must be greater than 0", "must be greater than 0"));
    }

    @Test
    @SuppressWarnings("java:S2699") // suppress: Add at least one assertion to this test case
    void exponentialBackoffConfig_ok() {
        StreamConsumerExponentialBackoffProperties config = StreamConsumerExponentialBackoffProperties.builder()
                .initialIntervalInSeconds(1)
                .multiplier(1.5)
                .maxIntervalInSeconds(30)
                .build();

        noValidationIssuesDetected(config);
    }

    @Override
    protected Class<?> getClassToTest() {
        return StreamConsumerExponentialBackoffProperties.class;
    }
}
