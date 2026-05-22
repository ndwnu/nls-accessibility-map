package nu.ndw.nls.accessibilitymap.backend.rabbitmq.factory;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamConsumerExponentialBackoffProperties;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamConsumerProperties;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamQueueProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.rabbit.stream.config.StreamRabbitListenerContainerFactory;
import org.springframework.rabbit.stream.listener.ConsumerCustomizer;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.util.backoff.ExponentialBackOff;

@RequiredArgsConstructor
@Slf4j
public class RabbitStreamListenerContainerFactoryFactoryBean implements
        FactoryBean<RabbitListenerContainerFactory<StreamListenerContainer>> {

    private final StreamQueueProperties queueConfig;

    private final Environment environment;

    @Override
    public RabbitListenerContainerFactory<StreamListenerContainer> getObject() {
        ExponentialBackOff backOff = getExponentialBackOff();

        StreamRabbitListenerContainerFactory factory = new StreamRabbitListenerContainerFactory(environment);
        factory.setNativeListener(true);

        factory.setAdviceChain(
                RetryInterceptorBuilder
                        .stateful()
                        .retryPolicy(
                                RetryPolicy.builder()
                                        .predicate(this::shouldRetry)
                                        .backOff(backOff)
                                        .build()
                        )
                        .build());
        factory.setConsumerCustomizer(createConsumerCustomizer());
        return factory;
    }

    @Override
    public Class<?> getObjectType() {
        return RabbitListenerContainerFactory.class;
    }

    private ExponentialBackOff getExponentialBackOff() {
        StreamConsumerExponentialBackoffProperties exponentialBackoffConfig = queueConfig.getConsumer().getExponentialBackoff();

        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setMaxInterval(exponentialBackoffConfig.getMaxIntervalInMilliSeconds());
        backOff.setInitialInterval(exponentialBackoffConfig.getInitialIntervalInMilliSeconds());
        backOff.setMultiplier(exponentialBackoffConfig.getMultiplier());
        return backOff;
    }

    private boolean shouldRetry(Throwable exception) {
        log.warn("Failed to process stream message", exception);
        String messageId = getFailedMessageId(exception);
        if (queueConfig.isMessageToSkip(messageId)) {
            log.info("Skip stream message with messageId: '{}'", messageId);
            return false;
        }
        return true;
    }

    private String getFailedMessageId(Throwable exception) {
        if (exception instanceof ListenerExecutionFailedException listenerExecutionFailedException) {
            return getMessageId(listenerExecutionFailedException);
        }
        return null;
    }

    private String getMessageId(ListenerExecutionFailedException exception) {
        Message failedMessage = exception.getFailedMessage();
        return Objects.isNull(failedMessage) ? null : failedMessage.getMessageProperties().getMessageId();
    }

    private ConsumerCustomizer createConsumerCustomizer() {
        StreamConsumerProperties consumerConfig = queueConfig.getConsumer();

        return (id, builder) -> builder.name(queueConfig.getStreamTrackingName())
                .stream(queueConfig.getStreamQueueName())
                .singleActiveConsumer()
                .offset(OffsetSpecification.first())
                .autoTrackingStrategy()
                .flushInterval(Duration.ofSeconds(consumerConfig.getFlushIntervalInSeconds()))
                .messageCountBeforeStorage(consumerConfig.getMessageCountBeforeStorage())
                .builder();
    }
}
