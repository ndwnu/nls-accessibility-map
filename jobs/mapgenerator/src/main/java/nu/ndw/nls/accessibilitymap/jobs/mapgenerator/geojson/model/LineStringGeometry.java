package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LineStringGeometry implements Geometry {

    private static final String TYPE = "LineString";

    private final List<List<Double>> coordinates;

    @Override
    public String getType() {
        return TYPE;
    }
}
