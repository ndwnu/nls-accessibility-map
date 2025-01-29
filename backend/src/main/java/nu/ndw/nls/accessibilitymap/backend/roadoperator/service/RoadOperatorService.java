package nu.ndw.nls.accessibilitymap.backend.roadoperator.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.RoadOperatorRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoadOperatorService {

    private final RoadOperatorRepository roadOperatorRepository;

    public List<RoadOperator> findAll() {

        return roadOperatorRepository.findAll();
    }
}
