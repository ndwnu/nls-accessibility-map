package nu.ndw.nls.accessibilitymap.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RoadSection {

    private final int roadSectionId;
    private Boolean forwardAccessible;
    private Boolean backwardAccessible;
}
