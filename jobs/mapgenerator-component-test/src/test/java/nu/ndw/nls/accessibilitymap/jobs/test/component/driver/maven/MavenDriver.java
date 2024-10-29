package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.maven;

import java.io.File;
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


    @Override
    public void prepareBeforeEachScenario() {

        buildRelatedProjectToComponentTest();
    }

    private void buildRelatedProjectToComponentTest() {

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

    @Override
    public void clearStateAfterEachScenario() {

    }
}
