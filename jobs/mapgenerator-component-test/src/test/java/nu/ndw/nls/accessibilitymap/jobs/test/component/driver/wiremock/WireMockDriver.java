package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.wiremock.configuration.WireMockConfiguration;
import org.springframework.stereotype.Component;

@Component
public class WireMockDriver implements StateManagement {

    public WireMockDriver(WireMockConfiguration wireMockConfiguration) {

        final var wireMock = WireMock.create()
                .host(wireMockConfiguration.getHost())
                .port(wireMockConfiguration.getPort())
                .build();

        WireMock.configureFor(wireMock);
    }

    @Override
    public void clearStateAfterEachScenario() {

        WireMock.reset();
    }
}
