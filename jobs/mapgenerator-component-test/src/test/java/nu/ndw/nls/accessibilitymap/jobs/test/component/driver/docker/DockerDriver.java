package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.process.ProcessManager;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.configuration.DockerDriverConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Mode;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerDriver implements StateManagement {

    private final DockerDriverConfiguration dockerDriverConfiguration;

    private final ProcessManager processManager;

    private final List<String> startedServices = new ArrayList<>();

    public void startService(String serviceName) {

        startedServices.add(serviceName);
        processManager.startProcessAndWaitToBeFinished(
                List.of("docker",
                        "compose",
                        "-f",
                        dockerDriverConfiguration.getComposeFile().getAbsolutePath(),
                        "up",
                        "-d",
                        serviceName));
    }

    public void startServiceAndWaitToBeFinished(String serviceName, Mode mode, List<Environment> environmentVariables) {

        startedServices.add(serviceName);
        ArrayList<String> commandArguments = new ArrayList<>();

        commandArguments.add("docker");
        commandArguments.add("compose");
        commandArguments.add("-f");
        commandArguments.add(dockerDriverConfiguration.getComposeFile().getAbsolutePath());
        commandArguments.add("run");
        commandArguments.add("--build");
        commandArguments.addAll(environmentVariables.stream()
                .map(environment -> List.of("-e", "%s=%s".formatted(environment.key(), environment.value())))
                .flatMap(List::stream)
                .toList());

        if (mode == Mode.DEBUG) {
            commandArguments.add("-e");
            commandArguments.add(
                    "JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005");

            commandArguments.add("-p");
            commandArguments.add("5005:5005");
        }

        commandArguments.add(serviceName);
        processManager.startProcessAndWaitToBeFinished(commandArguments);
    }

    private void stopService(String serviceName) {

        processManager.startProcessAndWaitToBeFinished(
                List.of("docker",
                        "compose",
                        "-f",
                        dockerDriverConfiguration.getComposeFile().getAbsolutePath(),
                        "rm",
                        "--stop",
                        "--force",
                        serviceName));

        startedServices.remove(serviceName);
    }

    @Override
    public void clearStateAfterEachScenario() {

        startedServices.stream().toList().forEach(this::stopService);
    }
}
