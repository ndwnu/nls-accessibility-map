package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GeoJsonOutputFormat;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.GenerateGeoJsonService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Slf4j
@Component
@Command(name = "generateGeoJson")
@RequiredArgsConstructor
public class GenerateGeoJsonCommand implements Callable<Integer> {

    private final GenerateGeoJsonService generateGeoJsonService;

    @Parameters(index = "0", description = "The GeoJson map type to generate.")
    private CmdGenerateGeoJsonType type;

    @Option(names = { "-f", "--format" }, description = "The output format of the GeoJson",
            defaultValue = "EFFECTIVELY_ACCESSIBLE_SPLIT")
    private GeoJsonOutputFormat outputFormat;

    @Option(names = { "-m", "--message" }, description = "Whether it should result into sending a Rabbit MQ message",
            defaultValue = "true")
    private boolean produceMessage;


    @Override
    public Integer call() {
        try {
            log.info("Generating GeoJson {} using accessibility network", type);
            generateGeoJsonService.generate(type, outputFormat, produceMessage);
            return 0;
        } catch (RuntimeException e) {
            log.error("An error occurred while generating GeoJson {} using accessibility network", type, e);
            return 1;
        }
    }
}
