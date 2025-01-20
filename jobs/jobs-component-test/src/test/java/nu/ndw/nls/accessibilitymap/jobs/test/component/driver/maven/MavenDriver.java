package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.maven;

import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.springboot.processrunner.runner.services.ProcessRunnerService;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MavenDriver implements StateManagement {

    private final MavenConfiguration mavenConfiguration;

    private final ProcessRunnerService processRunnerService;

    @Override
    public void prepareState() {

        buildRelatedProjectToComponentTest();
    }

    private void buildRelatedProjectToComponentTest() {

        processRunnerService.run(
                new File(mavenConfiguration.getRootPomRelativePath()).toPath(),
                List.of("pwd"),
                false);

        mavenConfiguration.getModulesUnderTest().forEach(moduleUnderTest -> {

            processRunnerService.run(
                    new File(mavenConfiguration.getRootPomRelativePath()).toPath(),
                    List.of("mvn",
                            "package",
                            "-DskipTests",
                            "-pl",
                            moduleUnderTest,
                            "-am"),
                    false);
        });
    }

    @Override
    public void clearState() {

    }
}
