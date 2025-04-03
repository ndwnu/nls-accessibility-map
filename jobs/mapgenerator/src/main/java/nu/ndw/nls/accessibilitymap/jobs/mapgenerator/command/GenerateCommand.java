package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services.MapGeneratorService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
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

    private final AccessibilityConfiguration accessibilityConfiguration;

    private final GenerateConfiguration generateConfiguration;

    private final ClockService clockService;

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
                            .trafficSignTypes(trafficSignTypes)
                            .startLocationLatitude(generateConfiguration.startLocationLatitude())
                            .startLocationLongitude(generateConfiguration.startLocationLongitude())
                            .searchRadiusInMeters(generateConfiguration.searchRadiusInMeters())
                            .trafficSignTextSignTypes(
                                    includeOnlyTimeWindowedSigns
                                            ? Set.of(TextSignType.TIME_PERIOD)
                                            : null)
                            .build())
                    .polygonMaxDistanceBetweenPoints(polygonMaxDistanceBetweenPoints)
                    .nwbVersion(accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion())
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
