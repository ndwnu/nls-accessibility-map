package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Feature {

    private static final String TYPE = "Feature";

    private final long id;

    private final Geometry geometry;

    private final Properties properties;

    public String getType() {
        return TYPE;
    }
}
