package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.MapGenerationJobDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.TrafficSignAnalyserJobDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.MapGeneratorJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;
import nu.ndw.nls.springboot.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Mode;

@RequiredArgsConstructor
public class DockerStepDefinitions {

    private final DockerDriver dockerDriver;

    private final MapGenerationJobDriver mapGenerationJobDriver;

    private final TrafficSignAnalyserJobDriver trafficSignAnalyserJobDriver;

    @Given("run container in background {word}")
    public void runContainerInBackground(String serviceName) {

        dockerDriver.startService(serviceName);
    }

    @Given("run container {word} in mode {word} with environment variables")
    public void runContainer(String serviceName, String mode, List<Environment> environmentVariables) {

        dockerDriver.startServiceAndWaitToBeFinished(serviceName, Mode.valueOf(mode.toUpperCase(Locale.US)),
                environmentVariables);
    }

    @When("run MapGenerationJob with configuration")
    public void runMapGenerationJob(List<MapGeneratorJobConfiguration> jobConfigurations) {

        jobConfigurations.forEach(jobConfiguration -> mapGenerationJobDriver.runMapGenerationJobDebugMode(jobConfiguration));
    }

    @When("run TrafficSignAnalyser with configuration")
    public void runTrafficSignAnalyser(List<TrafficSignAnalyserJobConfiguration> jobConfigurations) {

        jobConfigurations.forEach(jobConfiguration -> trafficSignAnalyserJobDriver.runTrafficSignAnalysisJob(jobConfiguration));
    }
}
