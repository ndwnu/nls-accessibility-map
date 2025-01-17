package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@Builder
@With
@Validated
public class RoadSectionFragment {

    @NotNull
    private Integer id;

    @NotNull
    private RoadSection roadSection;

    @NotNull
    @Valid
    private DirectionalSegment forwardSegment;

    @Valid
    private DirectionalSegment backwardSegment;

    public void setForwardSegment(@NotNull @Valid DirectionalSegment forwardSegment) {

        if (Objects.nonNull(this.forwardSegment)) {
            throw new IllegalStateException("forwardSegment has already been assigned. "
                    + "There should be always only one forwardSegment per RoadSectionFragment.");
        }
        this.forwardSegment = forwardSegment;
    }

    public void setBackwardSegment(@NotNull @Valid DirectionalSegment backwardSegment) {

        if (Objects.nonNull(this.backwardSegment)) {
            throw new IllegalStateException("backSegment has already been assigned. "
                    + "There should be always only one backSegment per RoadSectionFragment.");
        }
        this.backwardSegment = backwardSegment;
    }

    public List<DirectionalSegment> getSegments() {

        return Stream.of(forwardSegment, backwardSegment)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean isAccessibleFromAllSegments() {

        return getSegments().stream()
                .allMatch(DirectionalSegment::isAccessible);
    }

    public boolean isNotAccessibleFromAllSegments() {

        return getSegments().stream()
                .noneMatch(DirectionalSegment::isAccessible);
    }


    public boolean isPartiallyAccessible() {

        return getSegments().stream().anyMatch(DirectionalSegment::isAccessible)
                && getSegments().stream().anyMatch(directionalSegment -> !directionalSegment.isAccessible());
    }
}
