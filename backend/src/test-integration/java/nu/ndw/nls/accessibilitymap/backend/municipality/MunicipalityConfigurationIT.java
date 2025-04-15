package nu.ndw.nls.accessibilitymap.backend.municipality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.mappers.MunicipalityCoordinateMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.mappers.MunicipalityIdMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.mappers.MunicipalityMapperImpl;
import nu.ndw.nls.accessibilitymap.backend.yaml.YamlPropertySourceFactory;
import nu.ndw.nls.geometry.GeometryConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = MunicipalityConfiguration.class)
@EnableConfigurationProperties
@ExtendWith(MockitoExtension.class)
@Import({GeometryConfiguration.class, MunicipalityMapperImpl.class, MunicipalityCoordinateMapper.class, MunicipalityIdMapper.class,
        MunicipalityProperties.class, YamlPropertySourceFactory.class})
class MunicipalityConfigurationIT {

    @Autowired
    private MunicipalityConfiguration municipalityConfiguration;

    @Test
    void construction() {
        assertNotNull(municipalityConfiguration.getMunicipalities());
        assertEquals(342, municipalityConfiguration.getMunicipalities().size());
    }
}