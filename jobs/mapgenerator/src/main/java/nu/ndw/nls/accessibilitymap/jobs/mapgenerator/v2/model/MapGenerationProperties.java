package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.util.Set;
import lombok.Builder;

@Builder
public record MapGenerationProperties(Set<TrafficSignType> trafficSigns) {

}
