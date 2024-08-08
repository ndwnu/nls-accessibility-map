package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.*;

import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import org.junit.jupiter.api.Test;

class VehicleTypeVehiclePropertiesMapperTest {

    private final VehicleTypeVehiclePropertiesMapper vehicleTypeVehiclePropertiesMapper =
            new VehicleTypeVehiclePropertiesMapper();

    @Test
    void map_ok_c6() {
       assertEquals(VehicleProperties.builder()
               .carAccessForbidden(true)
               .build(), vehicleTypeVehiclePropertiesMapper.map(GenerateGeoJsonType.C6));
    }

    @Test
    void map_ok_c7() {
        assertEquals(VehicleProperties.builder()
                .hgvAccessForbidden(true)
                .build(), vehicleTypeVehiclePropertiesMapper.map(GenerateGeoJsonType.C7));
    }

    @Test
    void map_ok_c7b() {
        assertEquals(VehicleProperties.builder()
                .hgvAndBusAccessForbidden(true)
                .build(), vehicleTypeVehiclePropertiesMapper.map(GenerateGeoJsonType.C7B));
    }

    @Test
    void map_ok_c12() {
        assertEquals(VehicleProperties.builder()
                .motorVehicleAccessForbidden(true)
                .build(), vehicleTypeVehiclePropertiesMapper.map(GenerateGeoJsonType.C12));
    }

    @Test
    void map_ok_c22c() {
        assertEquals(VehicleProperties.builder()
                .lcvAndHgvAccessForbidden(true)
                .build(), vehicleTypeVehiclePropertiesMapper.map(GenerateGeoJsonType.C22C));
    }


}