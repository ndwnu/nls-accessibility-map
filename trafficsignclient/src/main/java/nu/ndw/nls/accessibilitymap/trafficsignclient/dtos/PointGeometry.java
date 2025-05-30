package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PointGeometry implements Geometry {

    private static final String TYPE = "Point";

    List<Double> coordinates;

    @Override
    public String getType() {
        return TYPE;
    }
}
