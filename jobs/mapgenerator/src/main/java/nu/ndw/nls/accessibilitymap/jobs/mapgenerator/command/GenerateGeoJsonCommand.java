package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties.VehiclePropertiesBuilder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.trafficsign.TrafficSignType;
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

    private final GenerateProperties generateProperties;

    @Option(names = {"-t", "--traffic-sign"},
            description = "Traffic sign to generate the map for.",
            required = true)
    private TrafficSignType trafficSignType;

    @Option(names = {"-tw", "--include-only-time-windowed-signs"},
            description = "Traffic sign to generate the map for.")
    private boolean includeOnlyTimeWindowedSigns;

    @Option(names = {"-p", "--publish-event"},
            description = "Whether it should publish results as events onto Rabbit MQ message",
            defaultValue = "true")
    private boolean publishEvents;

    @Override
    public Integer call() {
        try {
            GeoGenerationProperties geoGenerationProperties = GeoGenerationProperties.builder()
                    .trafficSignType(trafficSignType)
                    .vehicleProperties(buildVehicleProperties(trafficSignType))
                    .includeOnlyTimeWindowedSigns(includeOnlyTimeWindowedSigns)
                    .exportVersion(Integer.parseInt(LocalDateTime.now().toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)))
                    .nwbVersion(accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion())
                    .publishEvents(publishEvents)
                    .startLocationLatitude(generateProperties.getStartLocationLatitude())
                    .startLocationLongitude(generateProperties.getStartLocationLongitude())
                    .searchRadiusInMeters(generateProperties.getSearchRadiusInMeters())
                    .geoJsonProperties(
                            generateProperties.getGeoJsonProperties().get(trafficSignType))
                    .build();

            log.info("Generating GeoJson");
            mapGeneratorService.generate(geoGenerationProperties);
            return 0;
        } catch (RuntimeException e) {
            log.error("Could not generate GeoJson because of: ", e);
            return 1;
        }
    }

    private VehicleProperties buildVehicleProperties(TrafficSignType trafficSignType) {

        VehiclePropertiesBuilder vehiclePropertiesBuilder = VehicleProperties.builder();
        switch (trafficSignType) {
            case C6 -> vehiclePropertiesBuilder.carAccessForbiddenWt(true);
            case C7 -> vehiclePropertiesBuilder.hgvAccessForbiddenWt(true);
            case C7B -> vehiclePropertiesBuilder.hgvAndBusAccessForbiddenWt(true);
            case C12 -> vehiclePropertiesBuilder.motorVehicleAccessForbiddenWt(true);
            case C22C -> vehiclePropertiesBuilder.lcvAndHgvAccessForbiddenWt(true);
        }

        return vehiclePropertiesBuilder.build();
    }
}
