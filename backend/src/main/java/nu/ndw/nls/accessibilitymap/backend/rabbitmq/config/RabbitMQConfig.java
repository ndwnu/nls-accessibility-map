package nu.ndw.nls.accessibilitymap.backend.rabbitmq.config;

import com.rabbitmq.stream.Environment;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    @Bean
    public ScheduledExecutorService rabbitStreamExecutor() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("rabbit-stream-worker");
            return t;
        });
    }

    @Bean
    @Primary
    public Environment rabbitStreamEnvironment(ScheduledExecutorService rabbitStreamExecutor, RabbitProperties rabbitProperties) {

        return Environment.builder()
                .host(rabbitProperties.getHost())
                .port(rabbitProperties.getStream().getPort())
                .username(rabbitProperties.getUsername())
                .password(rabbitProperties.getPassword())
                .scheduledExecutorService(rabbitStreamExecutor)
                .build();
    }
}
