package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import java.util.List;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;

@Builder(toBuilder = true)
public record AccessibilityReason(String externalId, Direction direction, TrafficSignType trafficSignType, Integer roadSectionId,
                                  @With List<AccessibilityRestriction> restrictions) {


    public void mergeRestrictions(List<AccessibilityRestriction> restrictions) {
        this.restrictions.addAll(restrictions);
    }
}
