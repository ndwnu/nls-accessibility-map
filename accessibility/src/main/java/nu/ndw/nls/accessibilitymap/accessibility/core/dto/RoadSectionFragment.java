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
@Builder(toBuilder = true)
@With
@Validated
public class RoadSectionFragment {

    @NotNull
    private Integer id;

    @NotNull
    private RoadSection roadSection;

    @Valid
    private DirectionalSegment forwardSegment;

    @Valid
    private DirectionalSegment backwardSegment;


    public List<DirectionalSegment> getSegments() {

        return Stream.of(forwardSegment, backwardSegment)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean isAccessibleFromAnySegment() {

        return getSegments().stream()
                .anyMatch(DirectionalSegment::isAccessible);
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

    public boolean hasForwardSegment() {

        return Objects.nonNull(forwardSegment);
    }

    public boolean isForwardAccessible() {

        if (!hasForwardSegment()) {
            return false;
        } else {
            return forwardSegment.isAccessible();
        }
    }

    public boolean hasBackwardSegment() {

        return Objects.nonNull(backwardSegment);
    }

    public boolean isBackwardAccessible() {

        if (!hasBackwardSegment()) {
            return false;
        } else {
            return backwardSegment.isAccessible();
        }
    }
}
