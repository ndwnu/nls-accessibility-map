package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

@Getter
@Setter
@AllArgsConstructor
@Builder
public final class DirectionalSegment {

    @NotNull
    private final Direction direction;

    @NotNull
    private final LineString lineString;

    @NotNull
    private List<TrafficSign> trafficSigns;

    @NotNull
    private final Boolean accessible;
}
