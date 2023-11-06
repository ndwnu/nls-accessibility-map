package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"name", "code", "townName"})
public final class CountyJsonDto {

    private final String name;
    private final String code;
    @JsonProperty("townname")
    private final String townName;
}
