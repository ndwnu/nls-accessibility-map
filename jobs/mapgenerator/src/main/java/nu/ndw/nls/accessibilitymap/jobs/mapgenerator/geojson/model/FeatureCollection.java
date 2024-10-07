package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class FeatureCollection {

    private static final String TYPE = "FeatureCollection";

    List<Feature> features;

    public String getType() {
        return TYPE;
    }

}
