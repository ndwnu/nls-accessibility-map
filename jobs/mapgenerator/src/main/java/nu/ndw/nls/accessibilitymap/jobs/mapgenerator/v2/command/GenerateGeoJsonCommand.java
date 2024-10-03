package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.command;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.LocalDateVersionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.MapGeneratorService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Component("GenerateGeoJsonCommandV2")
@Command(name = "generateGeoJsonV2")
@RequiredArgsConstructor
public class GenerateGeoJsonCommand implements Callable<Integer> {

    private final MapGeneratorService mapGeneratorService;
    private final LocalDateVersionMapper localDateVersionMapper;
    private final AccessibilityConfiguration accessibilityConfiguration;

    @Option(names = {"-t", "--traffic-sign"},
            description = "Traffic signs to generate the map for.",
            required = true)
    private Set<TrafficSignType> trafficSignTypes;

    @Option(names = {"-m", "--message"},
            description = "Whether it should result into sending a Rabbit MQ message",
            defaultValue = "true")
    private boolean produceMessage;

    @Override
    public Integer call() {
        try {
            MapGenerationProperties mapGeneratorProperties = MapGenerationProperties.builder()
                    .trafficSigns(trafficSignTypes)
                    .exportVersion(localDateVersionMapper.map(LocalDateTime.now().toLocalDate()))
                    .nwbVersion(accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion())
                    .produceMessage(produceMessage)
                    .build();

            log.info("Generating GeoJson");
            mapGeneratorService.generate(mapGeneratorProperties);
            return 0;
        } catch (RuntimeException e) {
            log.error("Could not generate GeoJson because of: ", e);
            return 1;
        }
    }
}
