package nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureCollection {

    private static final String TYPE = "FeatureCollection";

    private List<Feature> features;

    public String getType() {
        return TYPE;
    }

}
