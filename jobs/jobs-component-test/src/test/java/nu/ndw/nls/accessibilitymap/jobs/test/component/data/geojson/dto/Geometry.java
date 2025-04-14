package nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LineStringGeometry.class, name = LineStringGeometry.TYPE),
        @JsonSubTypes.Type(value = PointGeometry.class, name = PointGeometry.TYPE),
})
public interface Geometry {

    String getType();
}
