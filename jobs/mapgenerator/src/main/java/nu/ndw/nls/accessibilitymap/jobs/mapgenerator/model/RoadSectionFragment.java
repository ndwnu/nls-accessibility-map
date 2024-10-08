package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RoadSectionFragment {

    @NotNull
    private Integer id;

    private RoadSection roadSection;

    @Default
    @NotNull
    @Valid
    private final List<DirectionalSegment> forwardSegments = new ArrayList<>();

    @Default
    @NotNull
    @Valid
    private final List<DirectionalSegment> backwardSegments = new ArrayList<>();

    public List<DirectionalSegment> getSegments() {

        return Stream.concat(forwardSegments.stream(), backwardSegments.stream()).toList();
    }
}
