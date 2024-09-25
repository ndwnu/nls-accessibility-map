package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration-test")
class MapGeneratorServiceIT {


    @Autowired
    private MapGeneratorService service;

    @Test
    void getInaccessibleRoadSections_ok() {
        service.getInaccessibleRoadSections(CmdGenerateGeoJsonType.C6);
    }
}
