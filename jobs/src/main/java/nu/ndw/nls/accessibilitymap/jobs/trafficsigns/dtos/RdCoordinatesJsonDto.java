package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"x", "y"})
public final class RdCoordinatesJsonDto {

    @JsonProperty("x")
    private final String x;
    @JsonProperty("y")
    private final String y;
}
