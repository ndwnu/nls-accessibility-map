package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration.JobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration.JobDriverConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.dto.JobArgument;
import nu.ndw.nls.springboot.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobDriver implements StateManagement {

    private final DockerDriver dockerDriver;

    private final JobDriverConfiguration jobDriverConfiguration;

    private final JobActuatorDriver jobActuatorDriver;

    public void runJob(String jobName) {
        runJob(jobName, List.of());
    }

    public void runJob(String jobName, List<JobArgument> additionalArguments) {
        JobConfiguration jobConfiguration = jobDriverConfiguration.getJobConfiguration(jobName);
        Stream<JobArgument> commandArguments = Stream.concat(
                jobConfiguration.getArguments().stream(),
                additionalArguments.stream()
        );

        if (jobConfiguration.isRunAsService()) {
            Response<String, Void> response = jobActuatorDriver.runJob(
                    jobConfiguration.getCommand(),
                    commandArguments.toList(),
                    jobConfiguration.getServiceConfiguration());

            assertThat(response.status()).isEqualTo(HttpStatus.NO_CONTENT);
        } else {
            dockerDriver.startServiceAndWaitToBeFinished(
                    jobConfiguration.getDockerConfiguration().getServiceName(),
                    Stream.concat(
                            jobConfiguration.getDockerConfiguration().getEnvironmentVariables().stream(),
                            Stream.of(Environment.builder()
                                    .key("COMMAND")
                                    .value(createComment(jobConfiguration.getCommand(), commandArguments.toList()))
                                    .build())
                    ).toList());
        }
    }

    private static String createComment(String command, List<JobArgument> arguments) {

        return "%s %s".formatted(
                command,
                arguments.stream()
                        .map(jobArgument -> (jobArgument.getParameter() + (Objects.isNull(jobArgument.getValue())
                                ? ""
                                : "=" + jobArgument.getValue())).trim())
                        .collect(Collectors.joining(" "))
        ).trim();
    }

    @Override
    public void prepareState() {
        jobDriverConfiguration.getJobConfigurations()
                .forEach((name, jobConfiguration) -> jobActuatorDriver.waitForJobToBeReady(jobConfiguration));
    }

    @Override
    public void clearState() {

        // No state to clear.
    }
}
