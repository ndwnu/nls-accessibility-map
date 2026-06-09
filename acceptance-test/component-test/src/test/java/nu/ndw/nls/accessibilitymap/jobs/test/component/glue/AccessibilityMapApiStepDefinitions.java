package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.jsonunit.core.Option;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
public class AccessibilityMapApiStepDefinitions {

    private final JsonMapper jsonMapper;

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final TestDataProvider testDataProvider;

    @When("request accessibility geojson for {word}")
    public void requestAccessibilityGeoJsonForV2(String requestFile) {

        AccessibilityRequestJson accessibilityRequest = testDataProvider.readFromFile(
                "api/accessibility/v2/request/",
                requestFile + ".json",
                AccessibilityRequestJson.class);
        Response<AccessibilityRequestJson, String> response = accessibilityMapApiClient.getAccessibilityGeoJson(accessibilityRequest);

        assertThat(response.containsError())
                .withFailMessage(
                        "Failed to get accessibility geojson. Error: %s with body: %s",
                        response.error(),
                        response.body())
                .isFalse();
    }

    @Then("we expect accessibility geojson response {word}")
    public void expectAccessibilityGeoJsonResponseV2(String responseFile) {

        Response<AccessibilityRequestJson, String> actualResponse = accessibilityMapApiClient.getLastResponseForGetAccessibilityGeoJson();
        assertThat(actualResponse.headers().containsHeaderValue(HttpHeaders.CONTENT_ENCODING, "gzip")).isTrue();

        String expectedResponse = testDataProvider.readFromFile(
                "api/accessibility/v2/response",
                responseFile + ".geojson");

        assertThatJson(actualResponse.bodyAsString())
                // Ignore small floating point differences in coordinates local vs aks env
                .withTolerance(1e-6)
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedResponse);
    }

    @When("request accessibility geojson for {word} and expect result {word}")
    public void requestAccessibilityGeoJsonForV2AnVerifyTheResult(String requestFile, String responseFile) {

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    requestAccessibilityGeoJsonForV2(requestFile);
                    expectAccessibilityGeoJsonResponseV2(responseFile);
                });
    }
}
