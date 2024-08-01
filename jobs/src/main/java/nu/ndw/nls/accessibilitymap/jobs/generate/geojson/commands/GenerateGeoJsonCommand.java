package nu.ndw.nls.accessibilitymap.jobs.generate.geojson.commands;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.generate.geojson.model.GenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.generate.geojson.services.GenerateGeoJsonService;
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
    private GenerateGeoJsonType type;

    @Override
    public Integer call() {
        try {
            log.info("Generating GeoJson {} using accessibility network", type);
            generateGeoJsonService.generate(GenerateGeoJsonType.TRUCKS_FORBIDDEN);
            return 0;
        } catch (RuntimeException e) {
            log.error("An error occurred while generating GeoJson {} using accessibility network", type, e);
            return 1;
        }
    }
}
