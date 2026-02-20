package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import org.locationtech.jts.geom.LineString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
@With
@Validated
public final class DirectionalSegment {

    @NotNull
    // AKA the edgeKey
    private final Integer id;

    @NotNull
    private final Direction direction;

    @NotNull
    private final LineString lineString;

    @NotNull
    @Min(0)
    @Max(1)
    private final double startFraction;

    @NotNull
    @Min(0)
    @Max(1)
    private final double endFraction;

    @NotNull
    private RoadSectionFragment roadSectionFragment;

    @Valid
    private Restrictions restrictions;

    private final boolean accessible;

    public boolean hasRestrictions() {

        return Objects.nonNull(restrictions) && !restrictions.isEmpty();
    }
}
