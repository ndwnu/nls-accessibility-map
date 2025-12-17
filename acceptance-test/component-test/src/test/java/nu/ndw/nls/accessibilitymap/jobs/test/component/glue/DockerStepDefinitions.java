package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.When;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.DataAnalyserJobDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.JobDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.MapGenerationJobDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BaseNetworkAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.MapGeneratorJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;

@RequiredArgsConstructor
public class DockerStepDefinitions {

    private final MapGenerationJobDriver mapGenerationJobDriver;

    private final DataAnalyserJobDriver dataAnalyserJobDriver;

    private final JobDriver jobDriver;

    @When("run MapGenerationJob with configuration")
    public void runMapGenerationJob(List<MapGeneratorJobConfiguration> jobConfigurations) {

        jobConfigurations.forEach(mapGenerationJobDriver::runMapGenerationJobDebugMode);
    }

    @When("run DataAnalyser RabbitMQ is configured")
    public void runDataAnalyserJobConfigureRabbitMQ() {

        jobDriver.runJob("DataAnalyserConfigureRabbitMQ");
    }

    @When("run AsymmetricTrafficSignsAnalysis with configuration")
    public void runAsymmetricTrafficSignsAnalysis(List<TrafficSignAnalyserJobConfiguration> jobConfigurations) {

        jobConfigurations.forEach(dataAnalyserJobDriver::runAsymmetricTrafficSignsAnalysis);
    }

    @When("run BaseNetworkAnalyser with configuration")
    public void runBaseNetworkAnalyser(List<BaseNetworkAnalyserJobConfiguration> jobConfigurations) {

        jobConfigurations.forEach(dataAnalyserJobDriver::runBaseNetworkAnalysisJob);
    }

    @When("run TrafficSignUpdateCache")
    public void runTrafficSignAnalyser() {

        jobDriver.runJob("TrafficSignUpdateCache");
    }

    @When("run GraphhopperJob createOrUpdateNetwork is executed")
    public void runGraphhopperJobCreateOrUpdateNetwork() {

        jobDriver.runJob("GraphHopperCreateOrUpdateNetwork");
    }

    @When("run GraphhopperJob RabbitMQ is configured")
    public void runGraphhopperJobConfigureRabbitMQ() {

        jobDriver.runJob("GraphHopperConfigureRabbitMQ");
    }
}
