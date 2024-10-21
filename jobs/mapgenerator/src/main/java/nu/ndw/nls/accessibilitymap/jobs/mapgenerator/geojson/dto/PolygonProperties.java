package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PolygonProperties implements Properties {

    private final List<String> windowTimes;
}
