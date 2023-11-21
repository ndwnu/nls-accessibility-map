package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"wgs84", "rd", "placement", "side", "bearing", "nenTurningDirection", "fraction",
        "drivingDirection", "road", "county", "bgtCode"})
@JsonInclude(NON_NULL)
public final class LocationJsonDtoV3 {

    private final Wgs84JsonDto wgs84;
    private final RdCoordinatesJsonDto rd;
    private final String placement;
    private final String side;
    private final Integer bearing;
    @JsonProperty("nen_turning_direction")
    private final Integer nenTurningDirection;
    private final Double fraction;
    @JsonProperty("driving_direction")
    private final String drivingDirection;
    private final RoadJsonDtoV3 road;
    private final CountyJsonDto county;
    @JsonProperty("bgt_code")
    private final String bgtCode;
}
