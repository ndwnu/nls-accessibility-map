package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.GenerateGeoJsonService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Slf4j
@Component
@Command(name = "generateGeoJson")
@RequiredArgsConstructor
public class GenerateGeoJsonCommand implements Callable<Integer> {

    private final GenerateGeoJsonService generateGeoJsonService;

    @Parameters(index = "0", description = "The GeoJson map type to generate.")
    private CmdGenerateGeoJsonType type;

    @Override
    public Integer call() {
        try {
            log.info("Generating GeoJson {} using accessibility network", type);
            generateGeoJsonService.generate(type);
            return 0;
        } catch (RuntimeException e) {
            log.error("An error occurred while generating GeoJson {} using accessibility network", type, e);
            return 1;
        }
    }
}
