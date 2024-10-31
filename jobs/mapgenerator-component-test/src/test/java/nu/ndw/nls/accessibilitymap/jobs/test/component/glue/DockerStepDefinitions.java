package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Mode;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.NetworkDataService;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.MapGenerationJobDriver;

@RequiredArgsConstructor
public class DockerStepDefinitions {

    private final DockerDriver dockerDriver;

    private final NetworkDataService networkDataService;

    private final MapGenerationJobDriver mapGenerationJobDriver;

    @Given("run container in background {word}")
    public void runContainerInBackground(String serviceName) {

        dockerDriver.startService(serviceName);
    }

    @Given("run container {word} in mode {word} with environment variables")
    public void runContainer(String serviceName, String mode, List<Environment> environmentVariables) {

        dockerDriver.startServiceAndWaitToBeFinished(serviceName, Mode.valueOf(mode.toUpperCase(Locale.US)),
                environmentVariables);
    }

    @When("run MapGenerationJob for traffic sign {word} with start location at node {int}")
    public void runMapGenerationJob(String trafficSignType, int startNodeId) {

        mapGenerationJobDriver.runMapGenerationJobDebugMode(trafficSignType, networkDataService.findNodeById(startNodeId));
    }
}
