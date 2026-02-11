package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointGeometry implements Geometry {

    private static final String TYPE = "Point";

    private final List<Double> coordinates;

    @Override
    public String getType() {
        return TYPE;
    }
}
