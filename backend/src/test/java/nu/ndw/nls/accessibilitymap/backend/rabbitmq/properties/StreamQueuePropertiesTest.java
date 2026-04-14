package nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;

class StreamQueuePropertiesTest extends ValidationTest {

    @Test
    void isMessageToSkip_ok_nullId() {
        List<String> messagesToSkip = List.of("skip-message-id");
        StreamQueueProperties config = createStreamQueueProperties(messagesToSkip);

        assertThat(config.isMessageToSkip(null)).isTrue();
    }

    @Test
    void isMessageToSkip_ok_skipMessage() {
        List<String> messagesToSkip = List.of("skip-message-id");
        StreamQueueProperties config = createStreamQueueProperties(messagesToSkip);

        assertThat(config.isMessageToSkip("skip-message-id")).isTrue();
    }

    @Test
    void isMessageToSkip_ok_doNotSkip() {
        List<String> messagesToSkip = List.of("skip-message-id");
        StreamQueueProperties config = createStreamQueueProperties(messagesToSkip);

        assertThat(config.isMessageToSkip("do-not-skip-message-id")).isFalse();
    }

    @Test
    void streamQueueConfig_nok_invalidValues() {
        StreamQueueProperties config = StreamQueueProperties.builder().build();
        validate(
                config,
                List.of("streamQueueName", "consumer", "streamTrackingName", "maxSegmentSizeInKb", "maxAgeInDays"),
                List.of("must be greater than 0", "must be greater than 0", "must not be blank", "must not be blank", "must not be null"));
    }

    @Test
    @SuppressWarnings("java:S2699") // suppress: Add at least one assertion to this test case
    void streamQueueConfig_ok() {
        StreamQueueProperties config = StreamQueueProperties.builder()
                .streamTrackingName("testTrackingName")
                .streamQueueName("testQueueName")
                .maxAgeInDays(1)
                .maxSegmentSizeInKb(500)
                .consumer(
                        StreamConsumerProperties.builder()
                                .flushIntervalInSeconds(5)
                                .messageCountBeforeStorage(10)
                                .exponentialBackoff(
                                        StreamConsumerExponentialBackoffProperties.builder()
                                                .initialIntervalInSeconds(1)
                                                .multiplier(1.5)
                                                .maxIntervalInSeconds(30)
                                                .build())
                                .build())
                .messagesToSkip(List.of())
                .build();

        noValidationIssuesDetected(config);
    }

    @Override
    protected Class<?> getClassToTest() {
        return StreamQueueProperties.class;
    }

    private StreamQueueProperties createStreamQueueProperties(Collection<String> messagesToSkip) {
        return StreamQueueProperties.builder()
                .streamTrackingName("testTrackingName")
                .streamQueueName("testQueueName")
                .consumer(
                        StreamConsumerProperties.builder()
                                .flushIntervalInSeconds(0)
                                .messageCountBeforeStorage(0)
                                .exponentialBackoff(null)
                                .build())
                .messagesToSkip(messagesToSkip)
                .build();
    }
}
