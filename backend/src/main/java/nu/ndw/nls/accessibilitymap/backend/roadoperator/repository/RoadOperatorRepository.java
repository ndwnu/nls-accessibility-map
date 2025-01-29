package nu.ndw.nls.accessibilitymap.backend.roadoperator.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.dto.RoadOperator;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoadOperatorRepository {

    private final RoadOperatorStorage roadOperatorStorage;

    public List<RoadOperator> findAll() {

        return roadOperatorStorage.getRoadOperators();
    }
}
