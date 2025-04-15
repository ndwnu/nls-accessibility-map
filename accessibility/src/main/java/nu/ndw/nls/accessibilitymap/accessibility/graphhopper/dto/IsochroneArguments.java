package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import com.graphhopper.routing.weighting.Weighting;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public record IsochroneArguments(
        @NotNull Weighting weighting,
        double searchDistanceInMetres,
        Integer municipalityId) {

}

