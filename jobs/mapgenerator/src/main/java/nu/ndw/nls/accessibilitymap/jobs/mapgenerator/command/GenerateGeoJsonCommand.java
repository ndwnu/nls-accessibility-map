package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.mapper.VehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services.MapGeneratorService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Component
@Command(name = "generateGeoJson")
@RequiredArgsConstructor
public class GenerateGeoJsonCommand implements Callable<Integer> {

    private final MapGeneratorService mapGeneratorService;

    private final AccessibilityConfiguration accessibilityConfiguration;

    private final GenerateConfiguration generateProperties;

    private final VehiclePropertiesMapper vehiclePropertiesMapper;

    private final ClockService clockService;

    @Option(names = {"-t", "--traffic-sign"},
            description = "Traffic sign to generate the map for.",
            required = true)
    private TrafficSignType trafficSignType;

    @Option(names = {"-tw", "--include-only-time-windowed-signs"},
            description = "Traffic sign to generate the map for.",
            defaultValue = "false")
    private boolean includeOnlyTimeWindowedSigns;

    @Option(names = {"-p", "--publish-events"},
            description = "Whether it should publish results as events onto Rabbit MQ message",
            defaultValue = "false")
    private boolean publishEvents;

    @Option(names = {"-sllat", "--start-location-latitude"},
            description = "Start location",
            defaultValue = "false")
    private double startLocationLatitude;

    @Option(names = {"-sllon", "--start-location-longitude"},
            description = "Start longitude",
            defaultValue = "false")
    private double startLocationLongitude;

    @Override
    public Integer call() {

        try {
            OffsetDateTime startTime = clockService.now();
            GeoGenerationProperties geoGenerationProperties = GeoGenerationProperties.builder()
                    .startTime(startTime)
                    .startLocationLatitude(startLocationLatitude)
                    .startLocationLongitude(startLocationLongitude)
                    .trafficSignType(trafficSignType)
                    .vehicleProperties(vehiclePropertiesMapper.map(trafficSignType))
                    .includeOnlyTimeWindowedSigns(includeOnlyTimeWindowedSigns)
                    .exportVersion(Integer.parseInt(startTime.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)))
                    .nwbVersion(accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion())
                    .publishEvents(publishEvents)
                    .generateConfiguration(generateProperties)
                    .build();

            log.info("Generating GeoJson");
            mapGeneratorService.generate(geoGenerationProperties);
            return 0;
        } catch (RuntimeException exception) {
            log.error("Could not generate GeoJson because of: ", exception);
            return 1;
        }
    }
}
