package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class RoadOperatorStepDefinitions {

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final FileService fileService;

    @When("i request all road operators")
    public void requestAllRoadOperators() {
        Response<Void, String> response = accessibilityMapApiClient.getRoadOperators();
        assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    }

    @Then("it should match {word} road operators")
    public void verifyRoadOperators(String municipalitiesFile) {
        Response<Void, String> response = accessibilityMapApiClient.getLastResponseForGetRoadOperators();
        String expectedResult = fileService.readTestDataFromFile("api/roadOperators", municipalitiesFile, "json");

        assertThatJson(response.bodyAsString()).isEqualTo(expectedResult);
    }
}
