package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nu.ndw.nls.geojson.geometry.model.PointJson;

/*
 * A GeoJSON Feature Object with custom properties
 * see: https://datatracker.ietf.org/doc/html/rfc7946#section-3.2
 */
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({"type", "id", "geometry", "properties"})
@ToString
public class TrafficSignGeoJsonDto {

    private static final String TYPE = "Feature";

    private UUID id;

    private PointJson geometry;
    private TrafficSignPropertiesDto properties;

    public String getType() {
        return TYPE;
    }

}
