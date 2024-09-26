package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.geojson;

import java.util.List;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class AccessibilityGeoJsonFeatureCollection {

    private static final String TYPE = "FeatureCollection";

    List<AccessibilityGeoJsonFeature> features;

    public String getType() {
        return TYPE;
    }

}
