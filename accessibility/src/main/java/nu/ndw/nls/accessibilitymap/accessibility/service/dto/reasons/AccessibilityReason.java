package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import java.util.List;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;

@Builder
public record AccessibilityReason(TrafficSign trafficSign, @With List<AccessibilityRestriction> restrictions) {

}
