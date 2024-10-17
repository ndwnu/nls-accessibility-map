package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import org.locationtech.jts.geom.LineString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@Builder
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
    private final RoadSectionFragment roadSectionFragment;

    @Valid
    private final TrafficSign trafficSign;

    private final boolean accessible;

    public boolean hasTrafficSign() {

        return Objects.nonNull(trafficSign);
    }
}
