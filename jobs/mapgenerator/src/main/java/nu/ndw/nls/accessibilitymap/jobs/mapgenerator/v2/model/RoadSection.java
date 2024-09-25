package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import lombok.Builder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;

@Builder
public record RoadSection(long roadSectionId, DirectionalRoadSection forward, DirectionalRoadSection backward) {

}
