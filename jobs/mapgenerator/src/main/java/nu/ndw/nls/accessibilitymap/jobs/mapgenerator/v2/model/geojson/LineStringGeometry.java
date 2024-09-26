package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.geojson;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LineStringGeometry implements Geometry {

    private static final String TYPE = "LineString";

    List<List<Double>> coordinates;

    @Override
    public String getType() {
        return TYPE;
    }
}
