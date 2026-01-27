package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import java.util.List;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;

@Builder(toBuilder = true)
public record AccessibilityReason(
        String trafficSignExternalId,
        Direction direction,
        TrafficSignType trafficSignType,
        Integer roadSectionId,
        @SuppressWarnings("java:S3740")
        @With List<AccessibilityRestriction> restrictions) {

    @SuppressWarnings("java:S3740")
    public void mergeRestrictions(List<AccessibilityRestriction> restrictions) {

        this.restrictions.addAll(restrictions);
    }
}
