package nu.ndw.nls.accessibilitymap.backend.roadoperator.repository;

import java.io.IOException;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperators;
import nu.ndw.nls.accessibilitymap.backend.yaml.AbstractYamlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

@Repository
public class RoadOperatorRepository extends AbstractYamlRepository<RoadOperators> {

    @Autowired
    public RoadOperatorRepository(final Environment environment) throws IOException {

        super(environment, RoadOperators.class, "road-operators");
    }

    public List<RoadOperator> findAll() {

        return getData();
    }
}
