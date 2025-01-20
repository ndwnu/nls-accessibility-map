package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.VehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto.AnalyseProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.configuration.AnalyserConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service.TrafficSignAnalyserService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Component
@Command(name = "analyse")
@RequiredArgsConstructor
public class AnalyseCommand implements Callable<Integer> {

    private final AccessibilityConfiguration accessibilityConfiguration;

    private final AnalyserConfiguration analyserConfiguration;

    private final VehiclePropertiesMapper vehiclePropertiesMapper;

    private final ClockService clockService;

    private final TrafficSignAnalyserService trafficSignAnalyserService;

    @Option(names = {"-t", "--traffic-sign"},
            description = "Traffic signs to generate the map for.",
            required = true)
    private List<TrafficSignType> trafficSignTypes;

    @Option(names = {"-ri", "--report-issues"},
            description = "Whether it should report found issues",
            defaultValue = "false")
    private boolean reportIssues;

    @Override
    public Integer call() {

        try {
            OffsetDateTime startTime = clockService.now();

            for (TrafficSignType trafficSignType : trafficSignTypes) {
                AnalyseProperties analyseProperties = AnalyseProperties.builder()
                        .startTime(startTime)
                        .startLocationLatitude(analyserConfiguration.startLocationLatitude())
                        .startLocationLongitude(analyserConfiguration.startLocationLongitude())
                        .trafficSignType(trafficSignType)
                        .vehicleProperties(vehiclePropertiesMapper.map(List.of(trafficSignType), false))
                        .nwbVersion(accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion())
                        .searchRadiusInMeters(analyserConfiguration.searchRadiusInMeters())
                        .reportIssues(reportIssues)
                        .build();

                log.info("Analysing traffic sign: %s".formatted(trafficSignType));
                trafficSignAnalyserService.analyse(analyseProperties);
            }
            return 0;
        } catch (RuntimeException exception) {
            log.error("Could not analyse traffic signs because of:", exception);
            return 1;
        }
    }
}
