package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class AccessibilityGeoJsonFeature {

    private static final String TYPE = "Feature";

    long id;
    LineStringGeojson geometry;
    AccessibilityGeoJsonProperties properties;

    public String getType() {
        return TYPE;
    }

}