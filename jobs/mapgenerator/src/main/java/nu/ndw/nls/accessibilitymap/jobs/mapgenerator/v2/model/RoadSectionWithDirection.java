package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public final class RoadSectionWithDirection {

    @NotNull
    private final long roadSectionId;

    @NotNull
    private RoadSectionMetaData metaData;

    @NotNull
    private final DirectionalSegment forward;

    @NotNull
    private final DirectionalSegment backward;

}
