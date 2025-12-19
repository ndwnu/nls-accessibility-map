package nu.ndw.nls.accessibilitymap.jobs.data.analyser.command;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseNetworkConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.NetworkAnalyserService;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Component
@Command(name = "analyse-base-network")
@RequiredArgsConstructor
public class AnalyseBaseNetworkCommand implements Callable<Integer> {

    private final GraphHopperService graphHopperService;

    private final GraphhopperConfiguration graphhopperConfiguration;

    private final NetworkAnalyserService networkAnalyserService;

    private final MessageService messageService;

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

    @Override
    public Integer call() {

        return messageService.receive(NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED, this::start).getResult();
    }

    private Integer start(NlsEvent nlsEvent) {

        try {
            AnalyseNetworkConfiguration analyseNetworkConfiguration = AnalyseNetworkConfiguration.builder()
                    .startLocationLatitude(startLocationLatitude)
                    .startLocationLongitude(startLocationLongitude)
                    .searchRadiusInMeters(searchRadiusInMeters)
                    .nwbVersion(graphhopperConfiguration.getMetaData().nwbVersion())
                    .reportIssues(reportIssues)
                    .build();

            networkAnalyserService.analyse(graphHopperService.getNetworkGraphHopper(), analyseNetworkConfiguration);

            return 0;
        } catch (RuntimeException exception) {
            log.error("Could not analyse base network because of:", exception);
            return 1;
        }
    }
}
