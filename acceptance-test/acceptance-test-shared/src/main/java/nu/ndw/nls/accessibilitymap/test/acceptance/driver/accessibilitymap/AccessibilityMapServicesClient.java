package nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.springboot.test.component.driver.job.JobActuatorDriver;
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

        jobDriverConfiguration.getJobConfigurations().values()
                .stream()
                .findFirst()
                .ifPresent(jobConfiguration -> {
                    var jobResponse = jobActuatorDriver.triggerActuatorEndpoint("accessibility-map-cache-reload", jobConfiguration);
                    assertThat(jobResponse.containsError())
                            .withFailMessage("Reloading job caches failed. %s", jobResponse.error())
                            .isFalse();
                });
    }
}
