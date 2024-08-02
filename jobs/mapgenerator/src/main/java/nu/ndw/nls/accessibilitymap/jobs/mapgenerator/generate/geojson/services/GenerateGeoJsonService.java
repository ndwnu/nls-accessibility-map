package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GenerateGeoJsonService {

    public void generate(GenerateGeoJsonType type) {
        log.info("Generating geojson {} (still needs to be implemented)", type);
    }

}