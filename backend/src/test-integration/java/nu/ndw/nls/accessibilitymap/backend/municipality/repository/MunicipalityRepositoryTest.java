package nu.ndw.nls.accessibilitymap.backend.municipality.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MunicipalityRepository.class)
@EnableConfigurationProperties
@ExtendWith(MockitoExtension.class)
class MunicipalityRepositoryTest {

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @Test
    void loadFromConfigFile() {

        assertThat(municipalityRepository).isNotNull();

        List<Municipality> municipalities = municipalityRepository.findAll();
        assertThat(municipalities).hasSize(342);
    }
}