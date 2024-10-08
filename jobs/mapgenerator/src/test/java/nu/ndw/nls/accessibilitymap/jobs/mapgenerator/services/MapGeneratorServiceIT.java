package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.trafficsign.TrafficSignType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("staging")
@TestPropertySource(properties = {
        "graphhopper.dir=../../graphhopper"
})
@Disabled
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
                .vehicleProperties(VehicleProperties
                        .builder()
                        .motorVehicleAccessForbiddenWt(true)
                        .build())
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
