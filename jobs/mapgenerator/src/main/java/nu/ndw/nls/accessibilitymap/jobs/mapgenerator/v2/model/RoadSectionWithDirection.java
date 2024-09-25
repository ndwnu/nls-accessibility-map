package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public final class RoadSectionWithDirection {

    private final long roadSectionId;

    private RoadSectionMetaData metaData;

    private final DirectionalSegment forward;

    private final DirectionalSegment backward;

}
