package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.wiremock.configuration.WireMockConfiguration;
import org.springframework.stereotype.Service;

@Service
public class WireMockService implements StateManagement {

	public WireMockService(WireMockConfiguration wireMockConfiguration) {

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
