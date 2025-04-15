package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import com.graphhopper.routing.weighting.Weighting;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Point;

@Builder
public record IsochroneArguments(
        @NotNull Weighting weighting,
        @NotNull Point startPoint,
        double searchDistanceInMetres,
        Integer municipalityId) {

}

