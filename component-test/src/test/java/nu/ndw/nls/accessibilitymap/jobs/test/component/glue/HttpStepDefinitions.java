package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;

@RequiredArgsConstructor
@Slf4j
public class HttpStepDefinitions {

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final TestDataProvider testDataProvider;

    @When("{word} is requested by {word}")
    public void request(String path, String httpMethodString) {

        accessibilityMapApiClient.genericRequest(path, httpMethodString);
    }

    @Then("I expect a {int} response with body {word}")
    public void expectBody(int expectedStatusCode, String bodyFileLocation) {

        var response = accessibilityMapApiClient.getLastResponseForGenericRequest();

        assertThat(response.status().value()).isEqualTo(expectedStatusCode);
        assertThat(response.body()).hasToString(testDataProvider.readFromFile(bodyFileLocation));
    }
}
