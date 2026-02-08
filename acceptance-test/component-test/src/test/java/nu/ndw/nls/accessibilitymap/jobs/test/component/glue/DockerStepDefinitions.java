package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.When;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.DataAnalyserJobDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.MapGenerationJobDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BaseNetworkAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.MapGeneratorJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapServicesClient;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;
import nu.ndw.nls.springboot.test.component.state.StateManagement;

@RequiredArgsConstructor
public class DockerStepDefinitions implements StateManagement {

    private final MapGenerationJobDriver mapGenerationJobDriver;

    private final DataAnalyserJobDriver dataAnalyserJobDriver;

    private final JobDriver jobDriver;

    private final AccessibilityMapServicesClient accessibilityMapServicesClient;

    @When("run MapGenerationJob with configuration")
    public void runMapGenerationJob(List<MapGeneratorJobConfiguration> jobConfigurations) {

        jobConfigurations.forEach(mapGenerationJobDriver::runMapGenerationJobDebugMode);
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

        jobDriver.run("trafficSignUpdateCache");
        accessibilityMapServicesClient.reloadCaches();
    }

    @Override
    public void prepareState() {
        StateManagement.super.prepareState();

        jobDriver.run("configureRabbitMQ");
    }

    @Override
    public void clearState() {
        // Nothing to do.
    }
}
