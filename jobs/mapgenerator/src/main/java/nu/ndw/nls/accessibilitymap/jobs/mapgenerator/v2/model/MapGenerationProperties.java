package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public final class MapGenerationProperties {

    private int exportVersion;

    private int nwbVersion;

    private final Set<TrafficSignType> trafficSigns;
}
