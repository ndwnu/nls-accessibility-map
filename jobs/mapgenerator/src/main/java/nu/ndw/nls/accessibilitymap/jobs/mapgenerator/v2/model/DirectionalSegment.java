package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.With;
import org.locationtech.jts.geom.LineString;

@Getter
@Setter
@AllArgsConstructor
@Builder
@With
public final class DirectionalSegment {

    private final int id;

    @NonNull
    private final Direction direction;

    @NonNull
    private final LineString lineString;

    @NonNull
    private final RoadSection roadSection;

    @Default
    @NonNull
    private final List<TrafficSign> trafficSigns = new ArrayList<>();

    private final boolean accessible;

    public boolean hasTrafficSigns() {

        return !trafficSigns.isEmpty();
    }
}
