package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import lombok.Builder;

@Builder
public record RoadSectionWithDirection(long roadSectionId, DirectionalSegment forward, DirectionalSegment backward) {

}
