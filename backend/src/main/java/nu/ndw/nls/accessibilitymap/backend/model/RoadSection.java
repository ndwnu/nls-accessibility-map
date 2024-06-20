package nu.ndw.nls.accessibilitymap.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RoadSection {

    private final int roadSectionId;
    private final LineString geometry;
    private Boolean forwardAccessible;
    private Boolean backwardAccessible;
}
