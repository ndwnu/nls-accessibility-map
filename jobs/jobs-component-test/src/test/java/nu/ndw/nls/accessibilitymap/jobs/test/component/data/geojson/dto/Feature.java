package nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    private static final String TYPE = "Feature";

    private long id;

    private Geometry geometry;

    private Properties properties;

    public String getType() {
        return TYPE;
    }
}
