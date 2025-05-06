package nu.ndw.nls.accessibilitymap.backend.municipality.repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipalities;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.yaml.AbstractYamlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

@Repository
public class MunicipalityRepository extends AbstractYamlRepository<Municipalities> {

    @Autowired
    public MunicipalityRepository(final Environment environment) throws IOException {

        super(environment, Municipalities.class, "municipalities");
    }

    public Optional<Municipality> findFirstById(String municipalityId) {

        return getData().stream()
                .filter(municipality -> municipality.municipalityId().equals(municipalityId))
                .findFirst();
    }

    public List<Municipality> findAll() {
        return getData();
    }
}
