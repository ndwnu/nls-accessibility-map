package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import io.cucumber.java.en.Then;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.jsonunit.core.Option;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.MapGenerationJobDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;

@RequiredArgsConstructor
public class GeoJsonStepDefinitions {

    private final FileService fileService;

    private final MapGenerationJobDriver mapGenerationJobDriver;

    @SuppressWarnings("java:S3658")
    @Then("we expect an empty geojson feature collection")
    public void geojson() {

        String actualResult = mapGenerationJobDriver.getLastGeneratedGeoJson();
        String expectedResult = fileService.readTestDataFromFile("geojson", "EmptyFeatureCollection", "geojson");

        assertThatJson(actualResult)
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedResult);
    }

    @Then("we expect {word} geojson")
    public void expectGeoJson(String fileName) {

        String actualResult = (fileName.toLowerCase(Locale.US).endsWith("polygon")
                ? mapGenerationJobDriver.getLastGeneratedPolygonGeoJson()
                : mapGenerationJobDriver.getLastGeneratedGeoJson());
        String expectedResult = fileService.readTestDataFromFile("geojson", fileName, "geojson");

        assertThatJson(actualResult)
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedResult);
    }
}
