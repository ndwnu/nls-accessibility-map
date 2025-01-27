package nu.ndw.nls.accessibilitymap.backend.roadoperator.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadOperatorJson;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.dto.RoadOperator;
import org.springframework.stereotype.Component;

@Component
public class RoadOperatorMapper {

    public List<RoadOperatorJson> map(Collection<RoadOperator> roadOperators) {

        return roadOperators.stream()
                .map(roadOperator -> (RoadOperatorJson) RoadOperatorJson.builder()
                        .roadOperatorType(roadOperator.roadOperatorType())
                        .roadOperatorName(roadOperator.roadOperatorName())
                        .roadOperatorCode(roadOperator.roadOperatorCode())
                        .municipalityId(roadOperator.municipalityId())
                        .requestExemptionUrl(
                                Objects.nonNull(roadOperator.requestExemptionUrl()) ? roadOperator.requestExemptionUrl().toString() : null)
                        .build())
                .toList();
    }
}
