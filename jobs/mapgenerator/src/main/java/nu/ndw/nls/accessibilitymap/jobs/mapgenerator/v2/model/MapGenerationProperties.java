package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;

@Builder
public record MapGenerationProperties(@NotNull Set<TrafficSignType> trafficSigns) {

}
