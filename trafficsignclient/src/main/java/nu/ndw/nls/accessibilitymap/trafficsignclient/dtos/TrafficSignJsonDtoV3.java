package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@JsonPropertyOrder({"id", "ndwId", "type", "schemaVersion", "validated", "validatedOn", "userId", "organisationId",
        "rvvCode", "blackCode", "textSigns", "location", "details", "publicationTimestamp"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TrafficSignJsonDtoV3 {

    private final Integer id;
    @JsonProperty("ndw_id")
    private final UUID ndwId;
    private final String type;
    @JsonProperty("schema_version")
    private final String schemaVersion;
    private final String validated;
    @JsonProperty("validated_on")
    private final LocalDate validatedOn;
    @JsonProperty("user_id")
    private final Integer userId;
    @JsonProperty("organisation_id")
    private final Integer organisationId;
    @JsonProperty("rvv_code")
    private final String rvvCode;
    @JsonProperty("zone_code")
    private final ZoneCode zoneCode;
    @JsonProperty("black_code")
    private final String blackCode;
    @JsonProperty("text_signs")
    private final List<TextSignJsonDtoV3> textSigns;
    private final LocationJsonDtoV3 location;
    private final TrafficSignDetailsJsonDtoV3 details;
    @JsonProperty("publication_timestamp")
    private final Instant publicationTimestamp;
}