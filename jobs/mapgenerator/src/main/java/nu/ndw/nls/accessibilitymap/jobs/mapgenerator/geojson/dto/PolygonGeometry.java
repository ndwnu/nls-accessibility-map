package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PolygonGeometry implements Geometry {

    private static final String TYPE = "Polygon";

    @JsonProperty("bbox")
    private final List<List<Double>> boundedBox;

    @NotNull
    @NotEmpty
    private final List<List<List<Double>>> coordinates;

    @Override
    public String getType() {
        return TYPE;
    }
}
