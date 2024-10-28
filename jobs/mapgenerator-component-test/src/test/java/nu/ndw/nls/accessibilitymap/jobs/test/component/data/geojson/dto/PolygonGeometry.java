package nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolygonGeometry implements Geometry {

    private static final String TYPE = "Polygon";

    @JsonProperty("bbox")
    private List<List<Double>> boundedBox;

    @NotNull
    @NotEmpty
    private List<List<List<Double>>> coordinates;

    @Override
    public String getType() {
        return TYPE;
    }
}
