package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.dto;

import java.util.List;
import lombok.Builder;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;

@Builder
public record Accessibility(
        List<IsochroneMatch> noAppliedRestrictions,
        List<IsochroneMatch> accessibilityWithRestrictions,
        List<IsochroneMatch> mergedAccessibility
) {

}
