package nu.ndw.nls.accessibilitymap.backend.rabbitmq.factory;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.codec.QpidProtonCodec;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamConsumerExponentialBackoffProperties;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamConsumerProperties;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamQueueProperties;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.rabbit.stream.config.StreamRabbitListenerContainerFactory;
import org.springframework.rabbit.stream.listener.ConsumerCustomizer;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.MethodInvocationRetryListenerSupport;
import org.springframework.retry.support.Args;
import org.springframework.retry.support.RetryTemplate;

@RequiredArgsConstructor
@Slf4j
public class RabbitStreamListenerContainerFactoryFactoryBean
        implements FactoryBean<RabbitListenerContainerFactory<StreamListenerContainer>> {

    private static final String METHOD_ARGS_ATTRIBUTE = "methodArgs";

    private final StreamQueueProperties queueConfig;

    private final Environment environment;

    @Override
    public RabbitListenerContainerFactory<StreamListenerContainer> getObject() {
        StreamConsumerExponentialBackoffProperties exponentialBackoffConfig = queueConfig.getConsumer().getExponentialBackoff();

        StreamRabbitListenerContainerFactory factory = new StreamRabbitListenerContainerFactory(environment);
        factory.setNativeListener(true);
        factory.setAdviceChain(
                RetryInterceptorBuilder
                        .stateless()
                        .retryOperations(
                                RetryTemplate.builder()
                                        .infiniteRetry()
                                        .exponentialBackoff(
                                                Duration.ofSeconds(exponentialBackoffConfig.getInitialIntervalInSeconds()),
                                                exponentialBackoffConfig.getMultiplier(),
                                                Duration.ofSeconds(exponentialBackoffConfig.getMaxIntervalInSeconds()))
                                        .withListener(skippableMessageRetryListener(queueConfig))
                                        .build())
                        .build());
        factory.setConsumerCustomizer(createConsumerCustomizer());
        return factory;
    }

    @Override
    public Class<?> getObjectType() {
        return RabbitListenerContainerFactory.class;
    }

    MethodInvocationRetryListenerSupport skippableMessageRetryListener(
            StreamQueueProperties streamQueueConfig) {
        return new MethodInvocationRetryListenerSupport() {
            @Override
            public <T, E extends Throwable> void onError(
                    RetryContext context, RetryCallback<T, E> callback,
                    Throwable throwable) {
                Message message = getMessage(context);
                log.warn(
                        "Failed to process stream message: '{}' for the {} time",
                        message.getProperties().getMessageId(),
                        context.getRetryCount(),
                        context.getLastThrowable());
                if (streamQueueConfig.isMessageToSkip(message.getProperties().getMessageId())) {
                    log.info("Skip stream message with messageId: '{}'", message.getProperties().getMessageId());
                    context.setExhaustedOnly();
                }
            }
        };
    }

    private Message getMessage(RetryContext context) {
        Object methodArgs = context.getAttribute(METHOD_ARGS_ATTRIBUTE);
        if (Objects.isNull(methodArgs) || !(methodArgs instanceof Args args)) {
            log.warn("No message provided in message listener retry handler with context: {}", context);
            return new QpidProtonCodec().messageBuilder().properties().messageBuilder().build();
        }
        return (Message) args.getArgs()[0];
    }

    private ConsumerCustomizer createConsumerCustomizer() {
        StreamConsumerProperties consumerConfig = queueConfig.getConsumer();

        return (id, builder) -> builder
                .name(queueConfig.getStreamTrackingName())
                .stream(queueConfig.getStreamQueueName())
                .singleActiveConsumer()
                .offset(com.rabbitmq.stream.OffsetSpecification.first())
                .autoTrackingStrategy()
                .flushInterval(Duration.ofSeconds(consumerConfig.getFlushIntervalInSeconds()))
                .messageCountBeforeStorage(consumerConfig.getMessageCountBeforeStorage())
                .builder();
    }
}
