package nu.ndw.nls.accessibilitymap.backend.rabbitmq.factory;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.OffsetSpecification;
import com.rabbitmq.stream.codec.QpidProtonCodec;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamConsumerExponentialBackoffProperties;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamConsumerProperties;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamQueueProperties;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryState;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;
import org.springframework.rabbit.stream.config.StreamRabbitListenerContainerFactory;
import org.springframework.rabbit.stream.listener.ConsumerCustomizer;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.util.backoff.ExponentialBackOff;

@RequiredArgsConstructor
@Slf4j
public class RabbitStreamListenerContainerFactoryFactoryBean
        implements FactoryBean<RabbitListenerContainerFactory<StreamListenerContainer>> {

    private final StreamQueueProperties queueConfig;

    private final Environment environment;

    @Override
    public RabbitListenerContainerFactory<StreamListenerContainer> getObject() {
        StreamConsumerExponentialBackoffProperties exponentialBackoffConfig = queueConfig.getConsumer().getExponentialBackoff();

        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(
                exponentialBackoffConfig.getInitialIntervalInSeconds(),
                exponentialBackoffConfig.getMultiplier());
        exponentialBackOff.setMaxInterval(exponentialBackoffConfig.getMaxIntervalInSeconds());

        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(Integer.MAX_VALUE)
                .backOff(exponentialBackOff)
                .build();

//        RetryTemplate retryTemplate = new RetryTemplate();
//        retryTemplate.setRetryPolicy(retryPolicy);
//        retryTemplate.setRetryListener(skippableMessageRetryListener(queueConfig));

        StreamRabbitListenerContainerFactory factory = new StreamRabbitListenerContainerFactory(environment);
        factory.setNativeListener(true);
        factory.setAdviceChain(
                RetryInterceptorBuilder
                        .stateless()
                        .retryPolicy(retryPolicy)
                        .build());
        factory.setConsumerCustomizer(createConsumerCustomizer());

        return factory;
    }

    @Override
    public Class<?> getObjectType() {
        return RabbitListenerContainerFactory.class;
    }

    RetryListener skippableMessageRetryListener(StreamQueueProperties streamQueueConfig) {
        return new RetryListener() {
            @Override
            public void onRetryableExecution(
                    RetryPolicy retryPolicy,
                    Retryable retryable,
                    RetryState retryState) {

                if (retryState.isSuccessful()) {
                    return;
                }

                Message message = getMessage(retryable);
                Object messageId = message.getProperties().getMessageId();

                log.warn(
                        "Failed to process stream message: '{}' for the {} time",
                        messageId,
                        retryState.getRetryCount(),
                        retryState.getLastException());

                if (streamQueueConfig.isMessageToSkip(messageId)) {
                    log.info("Skip stream message with messageId: '{}'", messageId);
//                    retryState.r();
                }
            }
        };
    }

    private Message getMessage(Retryable retryable) {
//
//        Object[] arguments = retryable.getArguments();
//
//        if (arguments == null || arguments.length == 0 || !(arguments[0] instanceof Message message)) {
//            log.warn("No message provided in message listener retry handler for retryable: {}", retryable);
//            return new QpidProtonCodec()
//                    .messageBuilder()
//                    .properties()
//                    .messageBuilder()
//                    .build();
//        }
//
//        return message;
        return null;
    }

    private ConsumerCustomizer createConsumerCustomizer() {
        StreamConsumerProperties consumerConfig = queueConfig.getConsumer();

        return (id, builder) -> builder
                .name(queueConfig.getStreamTrackingName())
                .stream(queueConfig.getStreamQueueName())
                .singleActiveConsumer()
                .offset(OffsetSpecification.first())
                .autoTrackingStrategy()
                .flushInterval(Duration.ofSeconds(consumerConfig.getFlushIntervalInSeconds()))
                .messageCountBeforeStorage(consumerConfig.getMessageCountBeforeStorage())
                .builder();
    }
}