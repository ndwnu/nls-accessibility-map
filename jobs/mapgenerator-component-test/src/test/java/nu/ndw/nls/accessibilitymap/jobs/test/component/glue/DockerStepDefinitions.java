package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.DockerDriver;

@RequiredArgsConstructor
public class DockerStepDefinitions {

    private final DockerDriver dockerDriver;

    @Given("run container in background {word}")
    public void runContainerInBackground(String serviceName) {

        dockerDriver.startService(serviceName);
//        dockerDriver.waitForServiceToBeHealthy(serviceName);

    }

    @Given("run container {word}")
    public void runContainer(String serviceName) {

        dockerDriver.startServiceAndWaitToBeFinished(serviceName);
//        dockerDriver.waitForServiceToBeHealthy(serviceName);

    }
}
