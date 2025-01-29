package nu.ndw.nls.accessibilitymap.backend.roadoperator.dto;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadOperatorJson.RoadOperatorTypeEnum;
import org.springframework.validation.annotation.Validated;

@Builder
@Validated
public record RoadOperator(
        @NotNull RoadOperatorTypeEnum roadOperatorType,
        @NotNull String roadOperatorCode,
        String municipalityId,
        @NotNull String roadOperatorName,
        URI requestExemptionUrl) {

}
