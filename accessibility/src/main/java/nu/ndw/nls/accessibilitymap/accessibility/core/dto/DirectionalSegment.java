package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
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
    private final Integer id;

    @NotNull
    private final Direction direction;

    @NotNull
    private final LineString lineString;

    @NotNull
    private RoadSectionFragment roadSectionFragment;

    @Valid
    private final List<TrafficSign> trafficSigns;

    private final boolean accessible;

    public long getRoadSectionId() {
        return roadSectionFragment.getRoadSection().getId();
    }

    public boolean hasTrafficSigns() {

        return Objects.nonNull(trafficSigns) && !trafficSigns.isEmpty();
    }
}
