package nu.ndw.nls.accessibilitymap.job.mapgenerator.command;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignCacheWatcher;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.services.MapGeneratorService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Component
@Command(name = "generate")
@RequiredArgsConstructor
public class GenerateCommand implements Callable<Integer> {

    private final MapGeneratorService mapGeneratorService;

    private final GenerateConfiguration generateConfiguration;

    private final ClockService clockService;

    private final TrafficSignCacheWatcher trafficSignCacheWatcher;

    @Option(names = {"-t", "--traffic-sign"},
            description = "Traffic signs to generate the map for.",
            required = true)
    private Set<TrafficSignType> trafficSignTypes;

    @Option(names = {"-e", "--export-type"},
            description = "Export types",
            required = true)
    private Set<ExportType> exportTypes;

    @Option(names = {"-n", "--export-name"},
            description = "Name for the generated file",
            required = true)
    private String exportName;

    @Option(names = {"-tw", "--include-only-time-windowed-signs"},
            description = "Traffic sign to generate the map for.",
            defaultValue = "false")
    private boolean includeOnlyTimeWindowedSigns;

    @Option(names = {"-p", "--publish-events"},
            description = "Whether it should publish results as events onto Rabbit MQ message",
            defaultValue = "false")
    private boolean publishEvents;

    @Option(names = {"-pmdbp", "--polygon-max-distance-between-points"},
            description = "The max instance between two point when calculating a polygon.",
            defaultValue = "0.0005")
    private double polygonMaxDistanceBetweenPoints;

    @Option(names = {"--start-location-latitude"},
            description = "Start location latitude",
            required = true)
    private double startLocationLatitude;

    @Option(names = {"--start-location-longitude"},
            description = "Start location longitude",
            required = true)
    private double startLocationLongitude;

    @Option(names = {"--search-radius-in-meters"},
            description = "Search radius in meters",
            required = true)
    private double searchRadiusInMeters;

    @Override
    public Integer call() {

        try {
            if (publishEvents && trafficSignTypes.size() > 1) {
                throw new NotImplementedException("Events are disabled for multiple traffic signs");
            }
            OffsetDateTime startTime = clockService.now();
            ExportProperties exportProperties = ExportProperties.builder()
                    .startTime(startTime)
                    .name(exportName)
                    .exportTypes(exportTypes)
                    .accessibilityRequest(AccessibilityRequest.builder()
                            .timestamp(startTime)
                            .trafficSignTypes(trafficSignTypes)
                            .startLocationLatitude(startLocationLatitude)
                            .startLocationLongitude(startLocationLongitude)
                            .searchRadiusInMeters(searchRadiusInMeters)
                            .trafficSignTextSignTypes(
                                    includeOnlyTimeWindowedSigns
                                            ? Set.of(TextSignType.TIME_PERIOD)
                                            : null)
                            .build())
                    .polygonMaxDistanceBetweenPoints(polygonMaxDistanceBetweenPoints)
                    .publishEvents(publishEvents)
                    .generateConfiguration(generateConfiguration)
                    .build();
            log.info("Generating export");
            mapGeneratorService.generate(exportProperties);
            return 0;
        } catch (RuntimeException exception) {
            log.error("Could not generate export because of: ", exception);
            return 1;
        }
    }
}
