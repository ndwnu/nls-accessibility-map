package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.configuration.GeneralConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.util.FileService;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration.MapGenerationJobDriverConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.JobConfiguration;
import nu.ndw.nls.springboot.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Mode;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapGenerationJobDriver implements StateManagement {

    private final GeneralConfiguration generalConfiguration;

    private final MapGenerationJobDriverConfiguration mapGenerationJobDriverConfiguration;

    private final DockerDriver dockerDriver;

    private final FileService fileService;

    private JobConfiguration lastJobExecution;

    public void runMapGenerationJobDebugMode(JobConfiguration jobConfiguration) {

        lastJobExecution = jobConfiguration;
        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-generator-jobs",
                generalConfiguration.isWaitForDebuggerToBeConnected() ? Mode.DEBUG : Mode.NORMAL,
                List.of(
                        Environment.builder()
                                .key("COMMAND")
                                .value(buildCommandArguments(jobConfiguration))
                                .build(),
                        Environment.builder()
                                .key("GRAPHHOPPER_NETWORKNAME")
                                .value("accessibility_latest_component_test")
                                .build(),
                        Environment.builder()
                                .key("NU_NDW_NLS_ACCESSIBILITYMAP_JOBS_GENERATE_STARTLOCATIONLATITUDE")
                                .value(String.valueOf(jobConfiguration.startNode().getLatitude()))
                                .build(),
                        Environment.builder()
                                .key("NU_NDW_NLS_ACCESSIBILITYMAP_JOBS_GENERATE_STARTLOCATIONLONGITUDE")
                                .value(String.valueOf(jobConfiguration.startNode().getLongitude()))
                                .build(),
                        Environment.builder()
                                .key("NU_NDW_NLS_ACCESSIBILITYMAP_TRAFFICSIGNCLIENT_API_TOWNCODES")
                                .value("TEST")
                                .build()));

    }

    public String buildCommandArguments(JobConfiguration jobConfiguration) {
        return "generate "
                + "--export-name=%s ".formatted(jobConfiguration.exportName())
                + createRepeatableArguments(jobConfiguration.trafficSignTypes(), jobConfiguration.exportTypes())
                + (jobConfiguration.includeOnlyWindowSigns() ? " --include-only-time-windowed-signs" : "")
                + (jobConfiguration.publishEvents() ? " --publish-events" : "")
                + (Objects.nonNull(jobConfiguration.polygonMaxDistanceBetweenPoints())
                ? " --polygon-max-distance-between-points=%s".formatted(jobConfiguration.polygonMaxDistanceBetweenPoints()) : "");
    }

    private static String createRepeatableArguments(Set<String> trafficSingTypes, Set<String> exportTypes) {

        return Stream.of(exportTypes.stream()
                                .map("--export-type=%s"::formatted),
                        trafficSingTypes.stream()
                                .map("--traffic-sign=%s"::formatted))
                .flatMap(i -> i)
                .collect(Collectors.joining(" "));
    }

    public String getLastGeneratedGeoJson() {

        return fileService.readDataFromFile(
                "%s/v1/windowTimes/%s/geojson".formatted(
                        mapGenerationJobDriverConfiguration.getLocationOnDisk(),
                        DateTimeFormatter.ofPattern("yyyyMMdd").format(OffsetDateTime.now())
                ),
                "%s%s".formatted(
                        lastJobExecution.exportName().toLowerCase(Locale.US),
                        lastJobExecution.includeOnlyWindowSigns() ? "WindowTimeSegments" : "")
                ,
                "geojson");
    }

    public String getLastGeneratedPolygonGeoJson() {

        return fileService.readDataFromFile(
                "%s/v1/windowTimes/%s/geojson".formatted(
                        mapGenerationJobDriverConfiguration.getLocationOnDisk(),
                        DateTimeFormatter.ofPattern("yyyyMMdd").format(OffsetDateTime.now())
                ),
                "%s%s-polygon".formatted(
                        lastJobExecution.exportName().toLowerCase(Locale.US),
                        lastJobExecution.includeOnlyWindowSigns() ? "WindowTimeSegments" : "")
                ,
                "geojson");
    }

    @Override
    public void clearStateAfterEachScenario() {

        lastJobExecution = null;
    }
}
