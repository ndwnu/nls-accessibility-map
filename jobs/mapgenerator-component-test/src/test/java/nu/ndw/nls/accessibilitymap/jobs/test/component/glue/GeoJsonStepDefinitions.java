package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import io.cucumber.java.en.Then;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.util.FileService;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.MapGenerationJobDriver;

@RequiredArgsConstructor
public class GeoJsonStepDefinitions {

    private final FileService testDataProvider;

    private final MapGenerationJobDriver mapGenerationJobDriver;

    @SuppressWarnings("java:S3658")
    @Then("we expect an empty geojson feature collection")
    public void geojson() {

        String actualResult = mapGenerationJobDriver.getLastGeneratedGeoJson();
        String expectedResult = testDataProvider.readTestDataFromFile("geojson", "EmptyFeatureCollection", "geojson");

        assertThatJson(actualResult).isEqualTo(expectedResult);
    }

    @Then("we expect {word} geojson")
    public void expectGeoJson(String fileName) {

        String actualResult = (fileName.toLowerCase(Locale.US).endsWith("polygon")
                ? mapGenerationJobDriver.getLastGeneratedPolygonGeoJson()
                : mapGenerationJobDriver.getLastGeneratedGeoJson());
        String expectedResult = testDataProvider.readTestDataFromFile("geojson", fileName, "geojson");

        assertThatJson(actualResult).isEqualTo(expectedResult);
    }
}
