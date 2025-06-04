package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Slf4j
public class HttpStepDefinitions {

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final TestDataProvider testDataProvider;

    @When("{word} is requested by {word}")
    public void request(String path, String httpMethodString) {

        accessibilityMapApiClient.request(path, httpMethodString);
    }

    @Then("I expect to be redirected to {word}")
    public void redirectExpectation(String locationHeaderValue) {

        Response response = accessibilityMapApiClient.getCache().getResponses().getLast();

        assertThat(response.status()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.headers()).containsEntry(HttpHeaders.LOCATION, List.of(locationHeaderValue));
    }

    @Then("I expect a {int} response with body {word}")
    public void expectBody(int expectedStatusCode, String bodyFileLocation) {

        Response response = accessibilityMapApiClient.getCache().getResponses().getLast();

        assertThat(response.status().value()).isEqualTo(expectedStatusCode);
        assertThat(response.body()).hasToString(testDataProvider.readFromFile(bodyFileLocation));
    }
}
