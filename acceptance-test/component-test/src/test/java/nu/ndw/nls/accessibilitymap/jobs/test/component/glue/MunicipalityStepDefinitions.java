package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class MunicipalityStepDefinitions {

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final FileService fileService;

    @When("i request all municipalities")
    public void requestAllMunicipalities() {
        Response<Void, MunicipalityFeatureCollectionJson> response = accessibilityMapApiClient.getMunicipalities();
        assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    }

    @Then("it should match {word} municipalities")
    public void verifyMunicipalities(String municipalitiesFile) {
        Response<Void, MunicipalityFeatureCollectionJson> response = accessibilityMapApiClient.getLastResponseForGetMunicipalities();
        String expectedResult = fileService.readTestDataFromFile("api/municipalities", municipalitiesFile, "geojson");

        assertThatJson(response.bodyAsString()).isEqualTo(expectedResult);
    }
}
