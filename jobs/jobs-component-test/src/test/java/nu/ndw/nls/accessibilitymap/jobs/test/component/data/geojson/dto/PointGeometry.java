package nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto;

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
public class PointGeometry implements Geometry {

    public static final String TYPE = "Point";

    private List<Double> coordinates;

    @Override
    public String getType() {
        return TYPE;
    }
}
