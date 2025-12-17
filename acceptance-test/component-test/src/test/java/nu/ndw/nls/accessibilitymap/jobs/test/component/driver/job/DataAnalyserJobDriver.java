package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import java.util.ArrayList;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.dto.JobArgument;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BaseNetworkAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataAnalyserJobDriver implements StateManagement {

    private final JobDriver jobDriver;

    public void runBaseNetworkAnalysisJob(BaseNetworkAnalyserJobConfiguration jobConfiguration) {
        jobDriver.runJob(
                "DataAnalyserBaseNetworkAnalysis",
                Stream.concat(
                        Stream.of(
                                JobArgument.builder()
                                        .parameter("--start-location-latitude")
                                        .value(String.valueOf(jobConfiguration.startNode().getLatitude()))
                                        .build(),
                                JobArgument.builder()
                                        .parameter("--start-location-longitude")
                                        .value(String.valueOf(jobConfiguration.startNode().getLongitude()))
                                        .build()),
                        buildArgumentsFromJobConfiguration(jobConfiguration)).toList()
        );
    }

    public void runAsymmetricTrafficSignsAnalysis(TrafficSignAnalyserJobConfiguration jobConfiguration) {
        jobDriver.runJob(
                "DataAnalyserAsymmetricTrafficSignsAnalysis",
                Stream.concat(
                        Stream.of(
                                JobArgument.builder()
                                        .parameter("--start-location-latitude")
                                        .value(String.valueOf(jobConfiguration.startNode().getLatitude()))
                                        .build(),
                                JobArgument.builder()
                                        .parameter("--start-location-longitude")
                                        .value(String.valueOf(jobConfiguration.startNode().getLongitude()))
                                        .build()),
                        buildArgumentsFromJobConfiguration(jobConfiguration)).toList()
        );
    }

    private Stream<JobArgument> buildArgumentsFromJobConfiguration(BaseNetworkAnalyserJobConfiguration jobConfiguration) {
        ArrayList<JobArgument> arguments = new ArrayList<>();
        if (jobConfiguration.reportIssues()) {
            arguments.add(JobArgument.builder().parameter("--report-issues").build());
        }
        return arguments.stream();
    }

    private Stream<JobArgument> buildArgumentsFromJobConfiguration(TrafficSignAnalyserJobConfiguration jobConfiguration) {
        ArrayList<JobArgument> arguments = new ArrayList<>();

        jobConfiguration.trafficSignGroups().forEach(trafficSignGroups -> {
            arguments.add(JobArgument.builder()
                    .parameter("--traffic-signs")
                    .value(String.join(",", trafficSignGroups))
                    .build());
        });

        if (jobConfiguration.reportIssues()) {
            arguments.add(JobArgument.builder().parameter("--report-issues").build());
        }
        return arguments.stream();
    }

    @Override
    public void clearState() {
        // no clean-up needed.
    }
}
