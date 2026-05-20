package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.shapes.BBox;
import lombok.Builder;

@Builder
public record IsochroneArguments(
        Weighting weighting,
        double searchDistanceInMetres,
        Integer municipalityId,
        BBox boundingBox) {

}

