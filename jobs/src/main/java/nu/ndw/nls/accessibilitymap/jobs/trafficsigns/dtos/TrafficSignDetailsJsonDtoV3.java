package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"image", "firstSeen", "lastSeen", "removed", "placed", "expected_removed", "expected_placed",
        "traffic_order_url"})
public final class TrafficSignDetailsJsonDtoV3 {

    private String image;
    @JsonProperty("first_seen")
    private String firstSeen;
    @JsonProperty("last_seen")
    private String lastSeen;
    private String removed;
    private String placed;
    @JsonProperty("expected_removed")
    private String expectedRemoved;
    @JsonProperty("expected_placed")
    private String expectedPlaced;
    @JsonProperty("traffic_order_url")
    private String trafficOrderUrl;
}
