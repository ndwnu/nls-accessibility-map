package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.shapes.BBox;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.RestrictionsIsochroneLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimit;

@Builder
public record IsochroneArguments(
        ExploreLimit<RestrictionsIsochroneLabel> exploreLimit,
        Weighting weighting,
        double searchDistanceInMetres,
        Integer municipalityId,
        BBox boundingBox,
        boolean reverseFlow) {

}

