package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model;

import lombok.Builder;
import lombok.Value;



@Value
@Builder
public class Feature {

    private static final String TYPE = "Feature";

    long id;

    Geometry geometry;

    Properties properties;

    public String getType() {
        return TYPE;
    }

}
