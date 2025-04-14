package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import nu.ndw.nls.geometry.stream.collectors.GeometryCollectors;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoJsonLineStringMergeMapper {

    private final JtsLineStringJsonMapper jtsLineStringJsonMapper;


    public LineStringJson mapToLineStringJson(List<LineString> lineStrings) {

        return lineStrings.stream()
                .collect(GeometryCollectors.mergeToLineString())
                .map(jtsLineStringJsonMapper::map)
                .orElseThrow(() -> new IllegalArgumentException("Cannot merge empty line strings"));

    }
}
