package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"name", "code", "townName"})
@JsonInclude(NON_NULL)
public final class CountyJsonDto {

    private final String name;
    private final String code;
    @JsonProperty("townname")
    private final String townName;
}
