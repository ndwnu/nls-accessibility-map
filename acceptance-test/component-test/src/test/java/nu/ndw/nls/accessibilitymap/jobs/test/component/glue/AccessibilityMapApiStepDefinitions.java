package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.jsonunit.core.Option;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BlockedRoadSection;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto.AccessibilityRequest;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;
import org.springframework.http.HttpHeaders;

@Slf4j
@RequiredArgsConstructor
public class AccessibilityMapApiStepDefinitions {

    private final ObjectMapper objectMapper;

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final TestDataProvider testDataProvider;

    @When("request accessibility for")
    public void requestAccessibilityFor(AccessibilityRequest accessibilityRequest) {
        var response = accessibilityMapApiClient.getAccessibilityForMunicipality(accessibilityRequest);

        assertThat(response.containsError())
                .withFailMessage("Failed to get accessibility for municipality. Error: %s with body: %s", response.error(), response.body())
                .isFalse();
    }

    @When("request accessibility geojson for")
    public void requestAccessibilityGeoJsonFor(AccessibilityRequest accessibilityRequest) {

        var response = accessibilityMapApiClient.getAccessibilityGeoJsonForMunicipality(accessibilityRequest);

        assertThat(response.containsError())
                .withFailMessage(
                        "Failed to get accessibility geojson for municipality. Error: %s with body: %s",
                        response.error(),
                        response.body())
                .isFalse();
    }

    @Then("we expect the following blocked roadSections with matched roadSection with id {int} and is forward accessible {word} and backward accessible {word}")
    public void weExpectTheFollowingBlockedRoadSections(
            int matchedRoadSectionId,
            String forwardAccessible,
            String backwardAccessible,
            List<BlockedRoadSection> blockedRoadSections
    ) throws JsonProcessingException {

        weExpectTheFollowingBlockedRoadSectionsWithReasons(
                matchedRoadSectionId,
                forwardAccessible,
                backwardAccessible,
                null,
                blockedRoadSections);
    }

    @Then("we expect the following blocked roadSections with matched roadSection with id {int} and is forward accessible {word} and backward accessible {word} with reasons {word}")
    public void weExpectTheFollowingBlockedRoadSectionsWithReasons(
            int matchedRoadSectionId,
            String forwardAccessible,
            String backwardAccessible,
            String reasonsFile,
            List<BlockedRoadSection> blockedRoadSections
    ) throws JsonProcessingException {
        Response<Void, AccessibilityMapResponseJson> response = accessibilityMapApiClient.getLastResponseForGetAccessibilityForMunicipality();

        String reasons = Objects.isNull(reasonsFile)
                ? "[]"
                : testDataProvider.readFromFile("api/accessibility/v1/InaccessibleReasons", reasonsFile + ".json");

        assertThatJson(response.bodyAsString())
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .inPath("$.matchedRoadSection")
                .isEqualTo("""
                        {
                            "roadSectionId" : %s,
                            "forwardAccessible" : %s,
                            "backwardAccessible" : %s,
                            "reasons": %s
                        }
                        """.formatted(
                        matchedRoadSectionId,
                        Boolean.parseBoolean(forwardAccessible),
                        Boolean.parseBoolean(backwardAccessible),
                        reasons
                ));

        assertThatJson(response.body())
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .inPath("$.inaccessibleRoadSections")
                .isEqualTo(objectMapper.writeValueAsString(blockedRoadSections));
    }

    @Then("we expect geojson to match {word}")
    public void weExpectGeojsonToMatchResponseAccessibilityGeojson(String expectedResponseFile) {

        Response<Void, RoadSectionFeatureCollectionJson> response = accessibilityMapApiClient.getLastResponseForGetAccessibilityGeoJsonForMunicipality();

        assertThatJson(response.body())
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(testDataProvider.readFromFile("api/accessibility/v1", expectedResponseFile));
    }

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
        assertThat(actualResponse.headers()).containsEntry(HttpHeaders.CONTENT_ENCODING, List.of("gzip"));

        String expectedResponse = testDataProvider.readFromFile(
                "api/accessibility/v2/response",
                responseFile + ".geojson");

        assertThatJson(actualResponse.bodyAsString()).isEqualTo(expectedResponse);
    }
}
