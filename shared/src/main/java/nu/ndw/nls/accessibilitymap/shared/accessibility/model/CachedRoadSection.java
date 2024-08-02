package nu.ndw.nls.accessibilitymap.shared.accessibility.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CachedRoadSection {

    private final int roadSectionId;
    private final LineString geometry;
    private boolean forwardAccessible;
    private boolean backwardAccessible;

}
