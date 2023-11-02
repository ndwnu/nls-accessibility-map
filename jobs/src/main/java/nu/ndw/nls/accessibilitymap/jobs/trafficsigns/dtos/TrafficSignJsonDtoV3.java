package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"id", "ndwId", "type", "schemaVersion", "validated", "validatedOn", "userId", "organisationId",
        "rvvCode", "blackCode", "textSigns", "location", "details", "publicationTimestamp"})
public final class TrafficSignJsonDtoV3 {

    private final Integer id;
    @JsonProperty("ndw_id")
    private final UUID ndwId;
    private final String type;
    @JsonProperty("schema_version")
    private final String schemaVersion;
    private final String validated;
    @JsonProperty("validated_on")
    private final Instant validatedOn;
    @JsonProperty("first_event_on")
    private final Instant firstEventOn;
    @JsonProperty("last_event_on")
    private final Instant lastEventOn;
    @JsonProperty("user_id")
    private final Integer userId;
    @JsonProperty("organisation_id")
    private final Integer organisationId;
    @JsonProperty("rvv_code")
    private final String rvvCode;
    @JsonProperty("black_code")
    private final String blackCode;
    @JsonProperty("text_signs")
    private final List<TextSignJsonDtoV3> textSigns;
    private final LocationJsonDtoV3 location;
    private final TrafficSignDetailsJsonDtoV3 details;
    @JsonFormat(shape = STRING)
    @JsonProperty("publication_timestamp")
    private final Instant publicationTimestamp;
}
