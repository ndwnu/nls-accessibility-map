package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.process.ProcessManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MavenDriver implements StateManagement {

    private final MavenConfiguration mavenConfiguration;

    private final ProcessManager processManager;

    private final List<String> startedServices = new ArrayList<>();

    public void buildRelatedProjectToComponentTest() {


        processManager.startProcessAndWaitToBeFinished(
                new File(mavenConfiguration.getRootPomRelativePath()),
                List.of("pwd"));

        processManager.startProcessAndWaitToBeFinished(
                new File(mavenConfiguration.getRootPomRelativePath()),
                List.of("mvn",
                        "package",
                        "-DskipTests",
                        "-pl",
                        mavenConfiguration.getModuleUnderTest(),
                        "-am"));

    }
//
//    public void startServiceAndWaitToBeFinished(String serviceName, Mode mode, List<Environment> environmentVariables) {
//
//        startedServices.add(serviceName);
//        ArrayList<String> commandArguments = new ArrayList<>();
//
//        commandArguments.add("docker-compose");
//        commandArguments.add("-f");
//        commandArguments.add(dockerDriverConfiguration.getComposeFile().getAbsolutePath());
//        commandArguments.add("run");
//        commandArguments.addAll(environmentVariables.stream()
//                .map(environment -> List.of("-e", "%s=%s".formatted(environment.key(), environment.value())))
//                .flatMap(List::stream)
//                .toList());
//
//        if (mode == Mode.DEBUG) {
//            commandArguments.add("-e");
//            commandArguments.add("JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005");
//
//            commandArguments.add("-p");
//            commandArguments.add("5005:5005");
//        }
//
//        commandArguments.add(serviceName);
//        processManager.startProcessAndWaitToBeFinished(commandArguments);
//
//    }
//
//    private void stopService(String serviceName) {
//
//        processManager.startProcessAndWaitToBeFinished(
//                List.of("docker-compose", "-f",
//                        dockerDriverConfiguration.getComposeFile().getAbsolutePath(),
//                        "rm",
//                        "--stop",
//                        "--force",
//                        serviceName));
//
//        startedServices.remove(serviceName);
//    }

    @Override
    public void clearStateAfterEachScenario() {

    }
}
