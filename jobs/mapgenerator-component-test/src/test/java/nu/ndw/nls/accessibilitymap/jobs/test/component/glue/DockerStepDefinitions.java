package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Mode;

@RequiredArgsConstructor
public class DockerStepDefinitions {

    private final DockerDriver dockerDriver;

    @Given("run container in background {word}")
    public void runContainerInBackground(String serviceName) {

        dockerDriver.startService(serviceName);
//        dockerDriver.waitForServiceToBeHealthy(serviceName);

    }

    @Given("run container {word} in mode {word} with environment variables")
    public void runContainer(String serviceName, String mode, List<Environment> environmentVariables) {

        dockerDriver.startServiceAndWaitToBeFinished(serviceName, Mode.valueOf(mode.toUpperCase(Locale.US)),
                environmentVariables);
    }

    @Then("run MapGenerationJob for traffic sign {word}")
    public void runMapGenerationJob(String trafficSignType) {

        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-generator-jobs",
                Mode.NORMAL, List.of(
                        Environment.builder()
                                .key("COMMAND")
                                .value("generateGeoJson --traffic-sign=%s --include-only-time-windowed-signs --publish-events".formatted(
                                        trafficSignType))
                                .build()));
    }

    @Then("run MapGenerationJob for traffic sign {word} in debug mode")
    public void runMapGenerationJobDebugMode(String trafficSignType) {

        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-generator-jobs",
                Mode.DEBUG, List.of(
                        Environment.builder()
                                .key("COMMAND")
                                .value("generateGeoJson --traffic-sign=%s --include-only-time-windowed-signs --publish-events".formatted(
                                        trafficSignType))
                                .build()));
    }
}
