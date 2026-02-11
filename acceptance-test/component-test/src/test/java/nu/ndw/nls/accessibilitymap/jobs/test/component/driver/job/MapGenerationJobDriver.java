package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration.MapGenerationJobDriverConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.MapGeneratorJobConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;
import nu.ndw.nls.springboot.test.component.driver.job.dto.JobArgument;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapGenerationJobDriver implements StateManagement {

    private final MapGenerationJobDriverConfiguration mapGenerationJobDriverConfiguration;

    private final FileService fileService;

    private final JobDriver jobDriver;

    private MapGeneratorJobConfiguration lastJobExecution;

    public void runMapGenerationJobDebugMode(MapGeneratorJobConfiguration jobConfiguration) {

        lastJobExecution = jobConfiguration;

        jobDriver.run(
                "job",
                Stream.concat(
                        Stream.of(
                                JobArgument.builder()
                                        .parameter("--start-location-latitude")
                                        .value(String.valueOf(jobConfiguration.startNode().getLatitude()))
                                        .build(),
                                JobArgument.builder()
                                        .parameter("--start-location-longitude")
                                        .value(String.valueOf(jobConfiguration.startNode().getLongitude()))
                                        .build(),
                                JobArgument.builder()
                                        .parameter("--search-radius-in-meters")
                                        .value("1000000000")
                                        .build()),
                        buildArgumentsFromJobConfiguration(jobConfiguration)).toList(),
                "mapGenerate"
        );
    }

    private Stream<JobArgument> buildArgumentsFromJobConfiguration(MapGeneratorJobConfiguration jobConfiguration) {
        ArrayList<JobArgument> arguments = new ArrayList<>();
        arguments.add(JobArgument.builder()
                .parameter("--export-name")
                .value(jobConfiguration.exportName())
                .build());

        jobConfiguration.trafficSignTypes().forEach(trafficSignType -> {
            arguments.add(JobArgument.builder()
                    .parameter("--traffic-sign")
                    .value(trafficSignType)
                    .build());
        });

        jobConfiguration.exportTypes().forEach(exportType -> {
            arguments.add(JobArgument.builder()
                    .parameter("--export-type")
                    .value(exportType)
                    .build());
        });

        if (jobConfiguration.includeOnlyWindowSigns()) {
            arguments.add(JobArgument.builder().parameter("--include-only-time-windowed-signs").build());
        }

        if (jobConfiguration.publishEvents()) {
            arguments.add(JobArgument.builder().parameter("--publish-events").build());
        }

        if (Objects.nonNull(jobConfiguration.polygonMaxDistanceBetweenPoints())) {
            arguments.add(JobArgument.builder()
                    .parameter("--polygon-max-distance-between-points")
                    .value(String.valueOf(jobConfiguration.polygonMaxDistanceBetweenPoints()))
                    .build());
        }

        return arguments.stream();
    }

    public String getLastGeneratedGeoJson() {

        return fileService.readDataFromFile(
                new File("%s/v1/windowTimes/%s/geojson/%s%s.geojson".formatted(
                        mapGenerationJobDriverConfiguration.getLocationOnDisk(),
                        DateTimeFormatter.ofPattern("yyyyMMdd").format(OffsetDateTime.now()),
                        lastJobExecution.exportName().toLowerCase(Locale.US),
                        lastJobExecution.includeOnlyWindowSigns() ? "WindowTimeSegments" : "")
                ));
    }

    public String getLastGeneratedPolygonGeoJson() {

        return fileService.readDataFromFile(
                new File("%s/v1/windowTimes/%s/geojson/%s%s-polygon.geojson".formatted(
                        mapGenerationJobDriverConfiguration.getLocationOnDisk(),
                        DateTimeFormatter.ofPattern("yyyyMMdd").format(OffsetDateTime.now()),
                        lastJobExecution.exportName().toLowerCase(Locale.US),
                        lastJobExecution.includeOnlyWindowSigns() ? "WindowTimeSegments" : "")
                ));
    }

    @Override
    public void clearState() {

        lastJobExecution = null;
    }
}
