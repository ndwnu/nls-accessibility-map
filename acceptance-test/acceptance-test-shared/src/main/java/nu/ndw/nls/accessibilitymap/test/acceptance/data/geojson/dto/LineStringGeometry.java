package nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineStringGeometry implements Geometry {

    public static final String TYPE = "LineString";

    private List<List<Double>> coordinates;

    @Override
    public String getType() {
        return TYPE;
    }
}
