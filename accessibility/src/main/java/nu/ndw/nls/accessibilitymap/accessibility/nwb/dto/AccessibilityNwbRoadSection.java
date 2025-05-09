package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;

/**
 * Summary of {@link nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto}, required for determining base accessibility
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class AccessibilityNwbRoadSection {

    private final long roadSectionId;

    private final LineString geometry;

    private boolean forwardAccessible;

    private boolean backwardAccessible;

}
