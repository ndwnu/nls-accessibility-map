package nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties;

import java.util.List;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;

class StreamConsumerPropertiesTest extends ValidationTest {

    @Test
    void consumerConfig_nok_invalidValues() {
        StreamConsumerProperties config = StreamConsumerProperties.builder()
                .build();

        validate(
                config,
                List.of("flushIntervalInSeconds", "messageCountBeforeStorage", "exponentialBackoff"),
                List.of("must be greater than 0", "must be greater than 0", "must not be null"));
    }

    @Test
    @SuppressWarnings("java:S2699") // suppress: Add at least one assertion to this test case
    void consumerConfig_ok() {
        StreamConsumerProperties config = StreamConsumerProperties.builder()
                .flushIntervalInSeconds(5)
                .messageCountBeforeStorage(10)
                .exponentialBackoff(StreamConsumerExponentialBackoffProperties.builder()
                        .initialIntervalInSeconds(1)
                        .multiplier(1.5)
                        .maxIntervalInSeconds(30)
                        .build())
                .build();

        noValidationIssuesDetected(config);
    }

    @Override
    protected Class<?> getClassToTest() {
        return StreamConsumerProperties.class;
    }
}
