package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.With;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;
import org.locationtech.jts.geom.LineString;

@Getter
@Setter
@AllArgsConstructor
@Builder
@With
public final class DirectionalSegment {

    @NonNull
    private final Integer id;

    @NonNull
    private final Direction direction;

    @NonNull
    private final LineString lineString;

    @NonNull
    private final RoadSectionFragment roadSectionFragment;

    private final TrafficSign trafficSign;

    private final boolean accessible;

    public boolean hasTrafficSign() {

        return Objects.nonNull(trafficSign);
    }
}
