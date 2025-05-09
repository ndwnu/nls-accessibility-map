package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.springboot.processrunner.runner.services.ProcessRunnerService;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.eclipse.sisu.PostConstruct;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MavenDriver implements StateManagement {

    private final MavenConfiguration mavenConfiguration;

    private final ProcessRunnerService processRunnerService;

    @PostConstruct
    public void prepareState() {

        buildRelatedProjectToComponentTest();
    }

    private void buildRelatedProjectToComponentTest() {

        int exitCode = processRunnerService.run(
                new File(mavenConfiguration.getRootPomRelativePath()).toPath(),
                Stream.concat(
                        Stream.of("mvn", "package", "-DskipTests", "-am"),
                        mavenConfiguration.getModulesUnderTest().stream()
                                .map(moduleUnderTest -> List.of("-pl", moduleUnderTest))
                                .flatMap(Collection::stream)
                ).toList(),
                false);

        assertThat(exitCode)
                .withFailMessage("Failed to build related project to component test. Check logs for details.")
                .isZero();
    }

    @Override
    public void clearState() {

    }
}
