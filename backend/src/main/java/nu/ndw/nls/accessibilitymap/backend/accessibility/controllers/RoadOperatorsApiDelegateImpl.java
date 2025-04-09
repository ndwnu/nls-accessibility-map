package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.RoadOperatorsApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadOperatorJson;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.mapper.RoadOperatorMapper;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.service.RoadOperatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadOperatorsApiDelegateImpl implements RoadOperatorsApiDelegate {

    private final RoadOperatorService roadOperatorService;

    private final RoadOperatorMapper roadOperatorMapper;

    @Override
    public ResponseEntity<List<RoadOperatorJson>> getRoadOperators() {

        return ResponseEntity.ok(roadOperatorMapper.map(roadOperatorService.findAll()));
    }
}
