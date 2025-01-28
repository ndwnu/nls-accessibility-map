package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command;

import java.time.OffsetDateTime;
import java.util.Arrays;
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
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.dtos.MessageConsumeResult;
import nu.ndw.nls.springboot.messaging.services.MessageService;
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

    private final MessageService messageService;

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

        MessageConsumeResult<Integer> result = messageService.receive(
                NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED, this::processMessage);

        return result.getResult();
    }

    private Integer processMessage(NlsEvent nlsEvent) {

        try {
            OffsetDateTime startTime = clockService.now();

            for (String trafficSignRvvCodes : trafficSigns) {
                List<TrafficSignType> trafficSignTypes = Arrays.stream(trafficSignRvvCodes.split(","))
                        .map(String::trim)
                        .map(TrafficSignType::valueOf)
                        .toList();

                AnalyseProperties analyseProperties = AnalyseProperties.builder()
                        .startTime(startTime)
                        .startLocationLatitude(analyserConfiguration.startLocationLatitude())
                        .startLocationLongitude(analyserConfiguration.startLocationLongitude())
                        .trafficSignTypes(trafficSignTypes)
                        .vehicleProperties(vehiclePropertiesMapper.map(trafficSignTypes, false))
                        .nwbVersion(accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion())
                        .searchRadiusInMeters(analyserConfiguration.searchRadiusInMeters())
                        .reportIssues(reportIssues)
                        .build();

                log.info("Analysing traffic signs: %s".formatted(trafficSignTypes));
                trafficSignAnalyserService.analyse(analyseProperties);
            }
            return 0;
        } catch (RuntimeException exception) {
            log.error("Could not analyse traffic signs because of:", exception);
            return 1;
        }
    }
}
