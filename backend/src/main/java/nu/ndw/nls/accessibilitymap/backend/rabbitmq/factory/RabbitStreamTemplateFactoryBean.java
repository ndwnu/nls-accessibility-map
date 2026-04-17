package nu.ndw.nls.accessibilitymap.backend.rabbitmq.factory;

import com.rabbitmq.stream.ByteCapacity;
import com.rabbitmq.stream.Environment;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamQueueProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;

@RequiredArgsConstructor
public class RabbitStreamTemplateFactoryBean implements FactoryBean<RabbitStreamTemplate> {

    private final StreamQueueProperties queueConfig;

    private final Environment environment;

    @Override
    public RabbitStreamTemplate getObject() {
        environment.streamCreator()
                .stream(queueConfig.getStreamQueueName())
                .maxSegmentSizeBytes(ByteCapacity.kB(queueConfig.getMaxSegmentSizeInKb()))
                .maxAge(Duration.of(queueConfig.getMaxAgeInDays(), ChronoUnit.DAYS))
                .create();

        return new RabbitStreamTemplate(environment, queueConfig.getStreamQueueName());
    }

    @Override
    public Class<?> getObjectType() {
        return RabbitStreamTemplate.class;
    }
}
