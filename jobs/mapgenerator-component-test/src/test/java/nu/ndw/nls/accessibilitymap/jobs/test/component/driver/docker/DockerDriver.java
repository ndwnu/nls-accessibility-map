package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.process.ProcessManager;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.configuration.DockerDriverConfiguration;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerDriver implements StateManagement {

    private final DockerDriverConfiguration dockerDriverConfiguration;

    private final ProcessManager processManager;

    private final List<String> startedServices = new ArrayList<>();


    @Override
    public void clearStateAfterEachScenario() {

        startedServices.stream().toList().forEach(this::stopService);
    }

//    public void waitForServiceToBeHealthy(String serviceName) {
//
//        startedServices.add(serviceName);
//
//        Process inspectProcess = processManager.startProcess(
//                List.of("docker",
//                        "inspect",
//                        "--format",
//                        "{{json .State.Health.Status }}",
//                        serviceName));
//    }


    public void startService(String serviceName) {

        startedServices.add(serviceName);
        processManager.startProcessAndWaitToBeFinished(
                List.of("docker-compose",
                        "-f",
                        dockerDriverConfiguration.getComposeFile().getAbsolutePath(),
                        "up",
                        "-d",
                        serviceName));

    }

    public void startServiceAndWaitToBeFinished(String serviceName) {

        startedServices.add(serviceName);
        processManager.startProcessAndWaitToBeFinished(
                List.of("docker-compose",
                        "-f",
                        dockerDriverConfiguration.getComposeFile().getAbsolutePath(),
                        "up",
                        serviceName));

    }

    private void stopService(String serviceName) {

        processManager.startProcessAndWaitToBeFinished(
                List.of("docker-compose", "-f",
                        dockerDriverConfiguration.getComposeFile().getAbsolutePath(),
                        "rm",
                        "--stop",
                        "--force",
                        serviceName));

        startedServices.remove(serviceName);
    }

}
