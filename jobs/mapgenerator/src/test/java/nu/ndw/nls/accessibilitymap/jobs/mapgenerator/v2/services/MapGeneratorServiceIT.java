package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.configuration.GenerateProperties;
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

    @Autowired
    private GenerateProperties generateProperties;

    @Test
    void generate_ok() {

        TrafficSignType trafficSignType = TrafficSignType.C12;
        GeoGenerationProperties mapGenerationProperties = GeoGenerationProperties.builder()
                .trafficSignType(trafficSignType)
                .exportVersion(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)))
                .includeOnlyTimeWindowedSigns(true)
                .nwbVersion(20240701)
                .startLocationLatitude(52.12096528507054)
                .startLocationLongitude(5.334845116067081)
                .searchRadiusInMeters(1000000)
                .geoJsonProperties(
                        generateProperties.getGeoJsonProperties().get(trafficSignType.name()))
                .build();

        service.generate(mapGenerationProperties);
    }
}
