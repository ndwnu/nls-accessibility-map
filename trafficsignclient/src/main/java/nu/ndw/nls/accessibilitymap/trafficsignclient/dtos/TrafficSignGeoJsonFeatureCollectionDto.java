package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/*
 * A GeoJSON Feature Object with custom properties
 * see: https://datatracker.ietf.org/doc/html/rfc7946#section-3.2
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class TrafficSignGeoJsonFeatureCollectionDto {

    private static final String TYPE = "FeatureCollection";

    private List<TrafficSignGeoJsonDto> features;

    public String getType() {
        return TYPE;
    }

}
