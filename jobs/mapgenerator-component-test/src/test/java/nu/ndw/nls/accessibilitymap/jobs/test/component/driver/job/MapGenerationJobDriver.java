package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.configuration.GeneralConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.util.FileDataProvider;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Mode;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Node;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration.MapGenerationJobDriverConfiguration;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapGenerationJobDriver implements StateManagement {

    private final GeneralConfiguration generalConfiguration;

    private final MapGenerationJobDriverConfiguration mapGenerationJobDriverConfiguration;

    private final DockerDriver dockerDriver;

    private final FileDataProvider fileDataProvider;

    private String lastJobExecutionTrafficSignType;

    private boolean lastJobExecutionIncludeOnlyWindowSigns;

    public void runMapGenerationJobDebugMode(String trafficSignType, Node startNode) {

        lastJobExecutionTrafficSignType = trafficSignType;
        lastJobExecutionIncludeOnlyWindowSigns = true;

        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-generator-jobs",
                generalConfiguration.isWaitForDebuggerToBeConnected() ? Mode.DEBUG : Mode.NORMAL,
                List.of(
                        Environment.builder()
                                .key("COMMAND")
                                .value(("generateGeoJson "
                                        + "--traffic-sign=%s "
                                        + "--include-only-time-windowed-signs "
                                        + "--publish-events "
                                        + "--start-location-latitude %s "
                                        + "--start-location-longitude %s").formatted(
                                        trafficSignType,
                                        startNode.getLatitude(),
                                        startNode.getLongitude()
                                        ))
                                .build()));
    }

    public String getLastGeneratedGeoJson() {

        return fileDataProvider.readDataFromFile(
                "%s/map-generation-destination/v1/windowTimes/%s/geojson".formatted(
                        mapGenerationJobDriverConfiguration.getLocationOnDisk().getAbsolutePath(),
                        DateTimeFormatter.ofPattern("yyyyMMdd").format(OffsetDateTime.now())
                ),
                "%s%s".formatted(
                        lastJobExecutionTrafficSignType.toLowerCase(Locale.US),
                        lastJobExecutionIncludeOnlyWindowSigns ? "WindowTimeSegments" : "")
                ,
                "geojson");
    }

    @Override
    public void clearStateAfterEachScenario() {

        lastJobExecutionTrafficSignType = null;
        lastJobExecutionIncludeOnlyWindowSigns = false;
    }
}
