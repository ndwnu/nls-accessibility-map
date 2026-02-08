package nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.springboot.test.component.driver.job.JobActuatorDriver;
import nu.ndw.nls.springboot.test.component.driver.job.configuration.JobConfiguration;
import nu.ndw.nls.springboot.test.component.driver.job.configuration.JobDriverConfiguration;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityMapServicesClient {

    private final JobActuatorDriver jobActuatorDriver;

    private final JobDriverConfiguration jobDriverConfiguration;

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    public void reloadCaches() {

        Response<Void, Void> response = accessibilityMapApiClient.reloadCache();
        assertThat(response.containsError())
                .withFailMessage("Reloading backend caches failed. %s", response.error())
                .isFalse();

        jobDriverConfiguration.getJobConfigurations().entrySet()
                .stream()
                .filter(entry -> !entry.getKey().contains("graphHopper"))
                .filter(entry -> !entry.getKey().contains("configureRabbitMQ"))
                .map(Map.Entry::getValue)
                .filter(JobConfiguration::isRunAsService)
                .forEach(jobConfiguration -> {
                    var jobResponse = jobActuatorDriver.triggerActuatorEndpoint("accessibility-map-cache-reload", jobConfiguration);
                    assertThat(jobResponse.containsError())
                            .withFailMessage("Reloading job caches failed. %s", jobResponse.error())
                            .isFalse();
                });
    }
}
