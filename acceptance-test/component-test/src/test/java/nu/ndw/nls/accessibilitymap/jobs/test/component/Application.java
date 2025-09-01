package nu.ndw.nls.accessibilitymap.jobs.test.component;

import nu.ndw.nls.springboot.messaging.MessagingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MessagingConfig.class)
public class Application {

    public static void main(final String[] arguments) {

        SpringApplication.run(Application.class, arguments);
    }
}
