package nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.RoadOperator.RoadOperatorTypeEnum;
import org.springframework.validation.annotation.Validated;

@Builder
@Validated
public record RoadOperator(
        @NotNull @JsonProperty("road-operator-type") RoadOperatorTypeEnum roadOperatorType,
        @NotNull @JsonProperty("road-operator-code") String roadOperatorCode,
        @JsonProperty("municipality-id")String municipalityId,
        @NotNull  @JsonProperty("road-operator-name")String roadOperatorName,
        @JsonProperty("request-exemption-url") URI requestExemptionUrl) {

}
