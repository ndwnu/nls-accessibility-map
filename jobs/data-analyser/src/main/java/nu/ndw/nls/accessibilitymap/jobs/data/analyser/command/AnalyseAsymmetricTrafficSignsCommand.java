package nu.ndw.nls.accessibilitymap.jobs.data.analyser.command;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignCacheUpdater;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseAsymmetricTrafficSignsConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.TrafficSignAnalyserService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Component
@Command(name = "analyse-asymmetric-traffic-signs")
@RequiredArgsConstructor
public class AnalyseAsymmetricTrafficSignsCommand implements Callable<Integer> {

    private final GraphHopperService graphHopperService;

    private final GraphhopperConfiguration graphhopperConfiguration;

    private final ClockService clockService;

    private final TrafficSignAnalyserService trafficSignAnalyserService;

    private final TrafficSignCacheUpdater trafficSignCacheUpdater;

    @Option(names = {"-t", "--traffic-signs"},
            description = "Traffic signs to generate the map for.",
            required = true)
    private List<String> trafficSigns;

    @Option(names = {"-ri", "--report-issues"},
            description = "Whether it should report found issues",
            defaultValue = "false")
    private boolean reportIssues;

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

    @Option(names = {"--update-traffic-sign-cache"},
            description = "Whether or not to update traffic sign cache. Only useful when running in as a service mode",
            defaultValue = "false")
    private boolean updateTrafficSignCache;

    @Override
    public Integer call() {

        if (updateTrafficSignCache) {
            trafficSignCacheUpdater.updateCache(graphHopperService.getNetworkGraphHopper());
        }

        try {
            OffsetDateTime startTime = clockService.now();

            for (String trafficSignRvvCodes : trafficSigns) {
                Set<TrafficSignType> trafficSignTypes = Arrays.stream(trafficSignRvvCodes.split(","))
                        .map(String::trim)
                        .map(TrafficSignType::valueOf)
                        .collect(Collectors.toSet());

                var analyseAsymmetricTrafficSignsConfiguration = AnalyseAsymmetricTrafficSignsConfiguration.builder()
                        .startTime(startTime)
                        .accessibilityRequest(AccessibilityRequest.builder()
                                .timestamp(startTime)
                                .trafficSignTypes(trafficSignTypes)
                                .startLocationLatitude(startLocationLatitude)
                                .startLocationLongitude(startLocationLongitude)
                                .searchRadiusInMeters(searchRadiusInMeters)
                                .build())
                        .nwbVersion(graphhopperConfiguration.getMetaData().nwbVersion())
                        .reportIssues(reportIssues)
                        .build();

                trafficSignAnalyserService.analyse(graphHopperService.getNetworkGraphHopper(), analyseAsymmetricTrafficSignsConfiguration);
            }

            return 0;
        } catch (RuntimeException exception) {
            log.error("Could not analyse traffic signs because of:", exception);
            return 1;
        }
    }
}
