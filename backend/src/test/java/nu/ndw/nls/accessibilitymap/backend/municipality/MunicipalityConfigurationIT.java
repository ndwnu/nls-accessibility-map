package nu.ndw.nls.accessibilitymap.backend.municipality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest()
@ActiveProfiles("test")
class MunicipalityConfigurationIT {

    @Autowired
    private MunicipalityConfiguration municipalityConfiguration;

    @Test
    void construction() {
        assertNotNull(municipalityConfiguration.getMunicipalities());
        assertEquals(342, municipalityConfiguration.getMunicipalities().size());
    }
}