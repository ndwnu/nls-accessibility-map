package nu.ndw.nls.accessibilitymap.backend.rabbitmq.factory;

import static com.rabbitmq.stream.OffsetSpecification.first;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rabbitmq.stream.ByteCapacity;
import com.rabbitmq.stream.ConsumerBuilder;
import com.rabbitmq.stream.ConsumerBuilder.AutoTrackingStrategy;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.StreamCreator;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBootTest
@SpringJUnitConfig(classes = {MessagingBeansRegistrar.class, ValidationAutoConfiguration.class})
class MessagingBeansRegistrarTest {

    @MockitoBean
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void whenContextLoads_thenStreamFactoryIsRegistered() {
        ConsumerBuilder consumerBuilder = mock(ConsumerBuilder.class);
        AutoTrackingStrategy autoTrackingStrategy = setupConsumerBuilderMock(consumerBuilder);
        when(environment.consumerBuilder()).thenReturn(consumerBuilder);

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext;
        assertThat(registry.containsBeanDefinition("updateRoadSectionStreamFactory")).isTrue();

        RabbitListenerContainerFactory<StreamListenerContainer> rabbitListenerContainerFactory =
                applicationContext.getBean("updateRoadSectionStreamFactory", RabbitListenerContainerFactory.class);
        MessageListenerContainer container = rabbitListenerContainerFactory.createListenerContainer();
        container.start();

        assertThat(container).isNotNull();
        verifyConsumerBuilderMock(consumerBuilder, autoTrackingStrategy);
    }

    @Test
    void whenContextLoads_thenStreamTemplateIsRegistered() {
        StreamCreator streamCreator = mock(StreamCreator.class);
        when(streamCreator.stream("nls_accessibility_map_update_road_section")).thenReturn(streamCreator);
        when(streamCreator.maxSegmentSizeBytes(ByteCapacity.kB(5000))).thenReturn(streamCreator);
        when(streamCreator.maxAge(Duration.ofDays(60))).thenReturn(streamCreator);
        when(environment.streamCreator()).thenReturn(streamCreator);

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext;
        assertThat(registry.containsBeanDefinition("updateRoadSectionStreamTemplate")).isTrue();
    }

    @Test
    void contextFailsToStartWhenConfigIsInvalid() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withUserConfiguration(MessagingBeansRegistrar.class, ValidationAutoConfiguration.class)
                .withPropertyValues(
                        "nu.ndw.nls.accessibilitymap.messaging.stream-queues.updateRoadSection.stream-tracking-name=updateRoadSection",
                        "nu.ndw.nls.accessibilitymap.messaging.stream-queues.updateRoadSection.stream-queue-name="
                        + "nls_accessibility_map_update_road_section"
                );

        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasMessage(
                            "Failed to bind properties under 'nu.ndw.nls.accessibilitymap.messaging' "
                            + "to nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.MessageStreamProperties")
                    .hasRootCauseInstanceOf(BindValidationException.class);
        });
    }

    private AutoTrackingStrategy setupConsumerBuilderMock(ConsumerBuilder consumerBuilder) {
        when(consumerBuilder.stream("nls_accessibility_map_update_road_section")).thenReturn(consumerBuilder);
        when(consumerBuilder.name("nls-accessibility-map-api-listener-v1")).thenReturn(consumerBuilder);
        when(consumerBuilder.singleActiveConsumer()).thenReturn(consumerBuilder);
        when(consumerBuilder.offset(first())).thenReturn(consumerBuilder);
        AutoTrackingStrategy autoTrackingStrategy = mock(AutoTrackingStrategy.class);
        when(consumerBuilder.autoTrackingStrategy()).thenReturn(autoTrackingStrategy);
        when(autoTrackingStrategy.flushInterval(Duration.ofSeconds(2))).thenReturn(autoTrackingStrategy);
        when(autoTrackingStrategy.messageCountBeforeStorage(1)).thenReturn(autoTrackingStrategy);
        when(autoTrackingStrategy.builder()).thenReturn(consumerBuilder);
        return autoTrackingStrategy;
    }

    private void verifyConsumerBuilderMock(ConsumerBuilder consumerBuilder, AutoTrackingStrategy autoTrackingStrategy) {
        verify(consumerBuilder).stream("nls_accessibility_map_update_road_section");
        verify(consumerBuilder).name("nls-accessibility-map-api-listener-v1");
        verify(consumerBuilder).singleActiveConsumer();
        verify(consumerBuilder).offset(first());
        verify(autoTrackingStrategy).flushInterval(Duration.ofSeconds(2));
        verify(autoTrackingStrategy).messageCountBeforeStorage(1);
        verify(autoTrackingStrategy).builder();
        verify(consumerBuilder).build();
    }
}
