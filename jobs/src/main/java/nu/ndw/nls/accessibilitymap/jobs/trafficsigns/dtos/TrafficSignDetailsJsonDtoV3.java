package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"imageUrl", "firstSeenOn", "lastSeenOn", "removedOn", "placedOn", "expectedPlacedOn",
        "expectedRemovedOn", "trafficOrderUrl"})
@JsonInclude(NON_NULL)
public final class TrafficSignDetailsJsonDtoV3 {

    @JsonProperty("image")
    private final String imageUrl;
    @JsonProperty("first_seen")
    private final LocalDate firstSeenOn;
    @JsonProperty("last_seen")
    private final LocalDate lastSeenOn;
    @JsonProperty("removed")
    private final LocalDate removedOn;
    @JsonProperty("placed")
    private final LocalDate placedOn;
    @JsonProperty("expected_placed")
    private final LocalDate expectedPlacedOn;
    @JsonProperty("expected_removed")
    private final LocalDate expectedRemovedOn;
    @JsonProperty("traffic_order_url")
    private final String trafficOrderUrl;
}
