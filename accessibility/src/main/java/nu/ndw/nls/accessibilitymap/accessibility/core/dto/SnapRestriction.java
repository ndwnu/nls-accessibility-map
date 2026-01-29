package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import com.graphhopper.storage.index.Snap;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;

public record SnapRestriction(Snap snap, Restriction restriction) {

}
