package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.jsonunit.core.Option;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BlockedRoadSection;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto.AccessibilityRequest;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;

@Slf4j
@RequiredArgsConstructor
public class AccessibilityMapApiStepDefinitions {

    private final ObjectMapper objectMapper;

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final TestDataProvider testDataProvider;

    @And("graphHopper data is reloaded")
    public void graphhopperDataIsReloaded() {

        Response<Void, Void> response = accessibilityMapApiClient.reloadGraphHopper();
        assertThat(response.containsError())
                .withFailMessage("Reloading graphhopper failed. %s", response.error())
                .isFalse();
    }

    @And("traffic signs data is reloaded")
    public void trafficSignsDataIsReloaded() {

        Response<Void, Void> response = accessibilityMapApiClient.reloadTrafficSigns();
        assertThat(response.containsError())
                .withFailMessage("Reloading traffic signs failed. %s", response.error())
                .isFalse();
    }

    @When("request accessibility for")
    public void requestAccessibilityFor(AccessibilityRequest accessibilityRequest) {

        Response<Void, AccessibilityMapResponseJson> response = accessibilityMapApiClient.getAccessibilityForMunicipality(
                accessibilityRequest);

        assertThat(response.containsError())
                .withFailMessage("Failed to get accessibility for municipality. Error: %s with body: %s", response.error(), response.body())
                .isFalse();
    }

    @When("request accessibility geojson for")
    public void requestAccessibilityGeoJsonFor(AccessibilityRequest accessibilityRequest) {

        Response<Void, RoadSectionFeatureCollectionJson> response = accessibilityMapApiClient.getAccessibilityGeoJsonForMunicipality(
                accessibilityRequest);

        assertThat(response.containsError())
                .withFailMessage("Failed to get accessibility geojson for municipality. Error: %s with body: %s",
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
                : testDataProvider.readFromFile("api/accessibility/InaccessibleReasons", reasonsFile + ".json");

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
                .isEqualTo(testDataProvider.readFromFile("api/accessibility", expectedResponseFile));
    }
}
