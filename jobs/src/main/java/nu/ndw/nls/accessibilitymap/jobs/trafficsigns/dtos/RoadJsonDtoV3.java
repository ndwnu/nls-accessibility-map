package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"name", "type", "number", "roadSectionId", "nwbVersion"})
public final class RoadJsonDtoV3 {

    private final String name;
    private final String type;
    private final String number;
    @JsonProperty("wvk_id")
    private final String roadSectionId;
    @JsonProperty("nwb_version")
    private final String nwbVersion;
}
