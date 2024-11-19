package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PolygonProperties implements Properties {

    private final List<String> windowTimes;

    @JsonProperty("roadSectionIds")
    private final List<Long> inAccessibleRoadSectionIds;
}
