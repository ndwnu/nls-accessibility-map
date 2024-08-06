package nu.ndw.nls.accessibilitymap.accessibility.model;

import com.graphhopper.routing.weighting.Weighting;
import java.util.Optional;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Point;

@Builder
public record IsochroneArguments(@NotNull Weighting weighting, @NotNull Point startPoint,
                                 double searchDistanceInMetres, Integer municipalityId) {

    public Optional<Integer> getMunicipalityId() {
        return Optional.ofNullable(municipalityId);
    }
}

