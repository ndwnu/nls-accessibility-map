package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.util.FileDataProvider;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.MapGenerationJobDriver;

@RequiredArgsConstructor
public class GeoJsonStepDefinitions {

    private final FileDataProvider testDataProvider;

    private final MapGenerationJobDriver mapGenerationJobDriver;

    @Then("we expect an empty geojson feature collection")
    public void geojson() {

        String actualResult = mapGenerationJobDriver.getLastGeneratedGeoJson();
        String expectedResult = testDataProvider.readTestDataFromFile("geojson", "EmptyFeatureCollection", "geojson");

        final ObjectMapper mapper = JsonMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .build();
        try {
            final var test = mapper.readValue(actualResult,
                    FeatureCollection.class);

            System.out.println(test);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        assertThatJson(actualResult).isEqualTo(expectedResult);
    }

    @Then("we expect the inner circle to be blocked")
    public void innerCircleToBeBlocked() {

        String actualResult = mapGenerationJobDriver.getLastGeneratedGeoJson();
        String expectedResult = testDataProvider.readTestDataFromFile("geojson", "InnerCircleBlocked", "geojson");

        assertThatJson(actualResult).isEqualTo(expectedResult);
    }
}
