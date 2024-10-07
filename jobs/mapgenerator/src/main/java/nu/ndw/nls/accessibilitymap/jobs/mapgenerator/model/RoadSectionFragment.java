package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RoadSectionFragment {

    private Integer id;

    private RoadSection roadSection;

    @Default
    @NonNull
    private final List<DirectionalSegment> forwardSegments = new ArrayList<>();

    @Default
    @NonNull
    private final List<DirectionalSegment> backwardSegments = new ArrayList<>();

    public boolean isOneWay() {
        return backwardSegments.isEmpty();
    }

    public List<DirectionalSegment> getSegments() {

        return Stream.concat(forwardSegments.stream(), backwardSegments.stream()).toList();
    }
}
