package nu.ndw.nls.accessibilitymap.jobs.trafficsign.command;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.command.dto.AnalyseProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.configuration.AnalyserConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.service.TrafficSignAnalyserService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Component
@Command(name = "analyse")
@RequiredArgsConstructor
public class AnalyseCommand implements Callable<Integer> {

    private final GraphHopperService graphHopperService;

    private final GraphhopperConfiguration graphhopperConfiguration;

    private final AnalyserConfiguration analyserConfiguration;

    private final ClockService clockService;

    private final TrafficSignAnalyserService trafficSignAnalyserService;

    @Option(names = {"-t", "--traffic-signs"},
            description = "Traffic signs to generate the map for.",
            required = true)
    private List<String> trafficSigns;

    @Option(names = {"-ri", "--report-issues"},
            description = "Whether it should report found issues",
            defaultValue = "false")
    private boolean reportIssues;

    @Override
    public Integer call() {

        try {
            OffsetDateTime startTime = clockService.now();

            for (String trafficSignRvvCodes : trafficSigns) {
                Set<TrafficSignType> trafficSignTypes = Arrays.stream(trafficSignRvvCodes.split(","))
                        .map(String::trim)
                        .map(TrafficSignType::valueOf)
                        .collect(Collectors.toSet());

                AnalyseProperties analyseProperties = AnalyseProperties.builder()
                        .startTime(startTime)
                        .accessibilityRequest(AccessibilityRequest.builder()
                                .timestamp(startTime)
                                .trafficSignTypes(trafficSignTypes)
                                .startLocationLatitude(analyserConfiguration.startLocationLatitude())
                                .startLocationLongitude(analyserConfiguration.startLocationLongitude())
                                .searchRadiusInMeters(analyserConfiguration.searchRadiusInMeters())
                                .build())
                        .nwbVersion(graphhopperConfiguration.getMetaData().nwbVersion())
                        .reportIssues(reportIssues)
                        .build();

                log.info("Analysing traffic signs: %s".formatted(trafficSignTypes.stream()
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new))));
                trafficSignAnalyserService.analyse(graphHopperService.getNetworkGraphHopper(), analyseProperties);
            }

            return 0;
        } catch (RuntimeException exception) {
            log.error("Could not analyse traffic signs because of:", exception);
            return 1;
        }
    }
}
