package nu.ndw.nls.accessibilitymap.jobs.test.component;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"nu.ndw.nls.routingmapmatcher", "nu.ndw.nls.accessibilitymap.jobs.test.component"})
public class Application {

	public static void main(final String[] arguments) {

		SpringApplication.run(Application.class, arguments);
	}
}
