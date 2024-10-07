package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.With;

@Getter
@Setter
@AllArgsConstructor
@Builder
@With
public final class RoadSection {

    private final long id;

    private RoadSectionMetaData metaData;

    @Default
    @NonNull
    private List<RoadSectionFragment> roadSectionFragments = new ArrayList<>();

}
