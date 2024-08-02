package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration
@ActiveProfiles(profiles = {"dev", "integration-test"})
class GeneratePropertiesIT {

    @Autowired
    private GenerateProperties generateProperties;


    @Test
    void configuration_ok_configExistsForEachGenerateGeoJsonType() {
        assertTrue(generateProperties.getGeojson().keySet().containsAll(List.of(GenerateGeoJsonType.values())),
                "There should be a configuration for each: " + GenerateGeoJsonType.class.getSimpleName());
    }
}