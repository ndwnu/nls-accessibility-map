package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class AccessibilityGeoJsonFeature {

    private static final String TYPE = "Feature";

    Long id;
    List<List<Double>> geometry;
    AccessibilityProperties properties;

    public String getType() {
        return TYPE;
    }

}
