package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "graphhopper.dir=../../graphhopper"
})
class MapGeneratorServiceIT {

    @Autowired
    private MapGeneratorService service;

    @Test
    void generate_ok() {

        MapGenerationProperties mapGenerationProperties = MapGenerationProperties.builder()
                .trafficSigns(Set.of(TrafficSignType.C6))
                .exportVersion(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)))
                .nwbVersion(20240701)
                .build();

        service.generate(mapGenerationProperties);
    }
}
