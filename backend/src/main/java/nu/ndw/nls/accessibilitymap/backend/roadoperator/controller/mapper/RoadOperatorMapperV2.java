package nu.ndw.nls.accessibilitymap.backend.roadoperator.controller.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RoadOperatorJson;
import org.springframework.stereotype.Component;

;

@Component
public class RoadOperatorMapperV2 {

    public List<RoadOperatorJson> map(Collection<RoadOperator> roadOperators) {
        return roadOperators.stream()
                .map(this::mapRoadOperator)
                .toList();
    }

    private RoadOperatorJson mapRoadOperator(RoadOperator roadOperator) {
        return RoadOperatorJson.builder()
                .roadOperatorType(roadOperator.roadOperatorType())
                .roadOperatorName(roadOperator.roadOperatorName())
                .roadOperatorCode(roadOperator.roadOperatorCode())
                .municipalityId(roadOperator.municipalityId())
                .requestExemptionUrl(Objects.toString(roadOperator.requestExemptionUrl(), null))
                .build();
    }
}
