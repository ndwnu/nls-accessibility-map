package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class MapGeneratorServiceIT {

    @Autowired
    private MapGeneratorService service;

    @Test
    void generate_ok() {

        MapGenerationProperties mapGenerationProperties = MapGenerationProperties.builder()
                .trafficSigns(Set.of(TrafficSignType.C12))
                .build();

        service.generate(mapGenerationProperties);
    }
}
