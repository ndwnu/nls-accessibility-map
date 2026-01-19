package nu.ndw.nls.accessibilitymap.backend.roadoperator.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.openapi.api.v2.RoadOperatorsV2ApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadOperatorJson;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.controller.mapper.RoadOperatorMapperV2;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.service.RoadOperatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadOperatorsV2ApiDelegateImpl implements RoadOperatorsV2ApiDelegate {

    private final RoadOperatorService roadOperatorService;

    private final RoadOperatorMapperV2 roadOperatorMapper;

    @Override
    public ResponseEntity<List<RoadOperatorJson>> getRoadOperators() {

        return ResponseEntity.ok(roadOperatorMapper.map(roadOperatorService.findAll()));
    }
}
