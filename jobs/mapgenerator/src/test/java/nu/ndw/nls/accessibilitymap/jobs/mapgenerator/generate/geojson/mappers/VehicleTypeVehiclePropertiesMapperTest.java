package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.*;

import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import org.junit.jupiter.api.Test;

class VehicleTypeVehiclePropertiesMapperTest {

    private final VehicleTypeVehiclePropertiesMapper vehicleTypeVehiclePropertiesMapper =
            new VehicleTypeVehiclePropertiesMapper();

    @Test
    void map_ok_c6() {
       assertEquals(VehicleProperties.builder()
               .carAccessForbiddenWt(true)
               .build(), vehicleTypeVehiclePropertiesMapper.map(CmdGenerateGeoJsonType.C6));
    }

    @Test
    void map_ok_c7() {
        assertEquals(VehicleProperties.builder()
                .hgvAccessForbiddenWt(true)
                .build(), vehicleTypeVehiclePropertiesMapper.map(CmdGenerateGeoJsonType.C7));
    }

    @Test
    void map_ok_c7b() {
        assertEquals(VehicleProperties.builder()
                .hgvAndBusAccessForbiddenWt(true)
                .build(), vehicleTypeVehiclePropertiesMapper.map(CmdGenerateGeoJsonType.C7B));
    }

    @Test
    void map_ok_c12() {
        assertEquals(VehicleProperties.builder()
                .motorVehicleAccessForbiddenWt(true)
                .build(), vehicleTypeVehiclePropertiesMapper.map(CmdGenerateGeoJsonType.C12));
    }

    @Test
    void map_ok_c22c() {
        assertEquals(VehicleProperties.builder()
                .lcvAndHgvAccessForbiddenWt(true)
                .build(), vehicleTypeVehiclePropertiesMapper.map(CmdGenerateGeoJsonType.C22C));
    }


}