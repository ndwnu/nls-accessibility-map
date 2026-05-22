package nu.ndw.nls.accessibilitymap.backend.rabbitmq.factory;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.MessageStreamProperties;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamQueueProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.validation.ValidationBindHandler;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Slf4j
@Component
public class MessagingBeansRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private static final String PROPERTIES_PREFIX = "nu.ndw.nls.accessibilitymap.messaging";

    @SuppressWarnings("java:S3749") // suppress: Annotate this member with
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * For each {@link MessageStreamProperties#getStreamQueues()}, we create a {@link RabbitStreamTemplateFactoryBean} and a
     * {@link RabbitStreamListenerContainerFactoryFactoryBean}. We can only create one bean with {@link Bean} in a {@link Configuration}
     * class, therefor we use this {@link BeanDefinitionRegistryPostProcessor} instead. This processor runs very early in the bean
     * dependency management system and {@link ConfigurationProperties} beans are not yet available in this phase therefor we need to
     * manually construct and validate the {@link MessageStreamProperties}
     *
     * @param registry the bean definition registry used by the application context
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        MessageStreamProperties properties = Binder.get(environment)
                .bind(PROPERTIES_PREFIX, Bindable.of(MessageStreamProperties.class), new ValidationBindHandler(validator))
                .orElseThrow(() -> new IllegalStateException("Missing configuration prefix: " + PROPERTIES_PREFIX));

        Map<String, StreamQueueProperties> streamQueues = properties.getStreamQueues();

        streamQueues.forEach((beanPrefix, queueConfig) -> {
            registerTemplate(registry, beanPrefix, queueConfig);
            registerFactory(registry, beanPrefix, queueConfig);
        });
    }

    private void registerTemplate(BeanDefinitionRegistry registry, String beanPrefix, StreamQueueProperties queueConfig) {
        String beanName = beanPrefix + "StreamTemplate";
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }

        AbstractBeanDefinition definition = BeanDefinitionBuilder
                .genericBeanDefinition(RabbitStreamTemplateFactoryBean.class)
                .addConstructorArgValue(queueConfig)
                .getBeanDefinition();

        registry.registerBeanDefinition(beanName, definition);
    }

    private void registerFactory(BeanDefinitionRegistry registry, String beanPrefix, StreamQueueProperties queueConfig) {
        String beanName = beanPrefix + "StreamFactory";
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }

        AbstractBeanDefinition definition = BeanDefinitionBuilder
                .genericBeanDefinition(RabbitStreamListenerContainerFactoryFactoryBean.class)
                .addConstructorArgValue(queueConfig)
                .getBeanDefinition();

        registry.registerBeanDefinition(beanName, definition);
    }
}
