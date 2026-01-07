package nu.ndw.nls.accessibilitymap.backend.roadoperator.controller.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.RoadOperator;
import org.springframework.stereotype.Component;

;

@Component
public class RoadOperatorMapperV2 {

    public List<RoadOperator> map(Collection<nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator> roadOperators) {
        return roadOperators.stream()
                .map(this::mapRoadOperator)
                .toList();
    }

    private RoadOperator mapRoadOperator(nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator roadOperator) {
        return RoadOperator.builder()
                .roadOperatorType(roadOperator.roadOperatorType())
                .roadOperatorName(roadOperator.roadOperatorName())
                .roadOperatorCode(roadOperator.roadOperatorCode())
                .municipalityId(roadOperator.municipalityId())
                .requestExemptionUrl(Objects.toString(roadOperator.requestExemptionUrl(), null))
                .build();
    }
}
