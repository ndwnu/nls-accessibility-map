package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.configuration.GeneralConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;
import nu.ndw.nls.springboot.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Mode;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrafficSignAnalyserJobDriver implements StateManagement {

    private final GeneralConfiguration generalConfiguration;

    private final DockerDriver dockerDriver;

    public void runTrafficSignAnalysisJob(TrafficSignAnalyserJobConfiguration jobConfiguration) {

        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-traffic-sign-analyser-job",
                generalConfiguration.isWaitForDebuggerToBeConnected() ? Mode.DEBUG : Mode.NORMAL,
                List.of(
                        Environment.builder()
                                .key("COMMAND")
                                .value(buildAnalyseCommand(jobConfiguration))
                                .build(),
                        Environment.builder()
                                .key("GRAPHHOPPER_NETWORKNAME")
                                .value("accessibility_latest_component_test")
                                .build(),
                        Environment.builder()
                                .key("NU_NDW_NLS_ACCESSIBILITYMAP_JOBS_ANALYSE_STARTLOCATIONLATITUDE")
                                .value(String.valueOf(jobConfiguration.startNode().getLatitude()))
                                .build(),
                        Environment.builder()
                                .key("NU_NDW_NLS_ACCESSIBILITYMAP_JOBS_ANALYSE_STARTLOCATIONLONGITUDE")
                                .value(String.valueOf(jobConfiguration.startNode().getLongitude()))
                                .build(),
                        Environment.builder()
                                .key("NU_NDW_NLS_ACCESSIBILITYMAP_TRAFFICSIGNCLIENT_API_TOWNCODES")
                                .value("TEST")
                                .build()));
    }

    public String buildAnalyseCommand(TrafficSignAnalyserJobConfiguration jobConfiguration) {
        return "analyse "
                + createRepeatableArguments(jobConfiguration.trafficSignGroups())
                + (jobConfiguration.reportIssues() ? " --report-issues" : "");
    }

    private static String createRepeatableArguments(List<Set<String>> trafficSignGroups) {

        return trafficSignGroups.stream()
                .map(trafficSignGroup -> "--traffic-signs=%s".formatted(String.join(",", trafficSignGroup)))
                .collect(Collectors.joining(" "));
    }

    @Override
    public void clearState() {

    }
}
