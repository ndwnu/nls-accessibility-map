package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class FeatureCollection {

    private static final String TYPE = "FeatureCollection";

    private final List<Feature> features;

    public String getType() {
        return TYPE;
    }

}
