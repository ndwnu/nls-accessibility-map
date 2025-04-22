package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import org.locationtech.jts.geom.LineString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
@With
@Validated
public final class RoadSection {

    @NotNull
    private final Long id;

    @Default
    @NotEmpty
    @Valid
    private List<RoadSectionFragment> roadSectionFragments = new ArrayList<>();

    public boolean isRestrictedInAnyDirection() {

        return roadSectionFragments.stream()
                .anyMatch(RoadSectionFragment::isNotAccessibleFromAllSegments)
                || roadSectionFragments.stream().anyMatch(RoadSectionFragment::isPartiallyAccessible);
    }

    public boolean hasForwardSegments() {

        return roadSectionFragments.stream()
                .map(RoadSectionFragment::getForwardSegment)
                .anyMatch(Objects::nonNull);
    }

    public boolean hasBackwardSegments() {

        return roadSectionFragments.stream()
                .map(RoadSectionFragment::getBackwardSegment)
                .anyMatch(Objects::nonNull);
    }

    public boolean isForwardAccessible() {

        return roadSectionFragments.stream()
                .allMatch(RoadSectionFragment::isForwardAccessible);
    }

    public boolean isBackwardAccessible() {

        return roadSectionFragments.stream()
                .allMatch(RoadSectionFragment::isBackwardAccessible);
    }

    public List<LineString> getForwardGeometries() {

        if (!hasForwardSegments()) {
            throw new IllegalStateException("No forward geometry found for road section %s".formatted(id));
        }

        return roadSectionFragments.stream()
                .map(RoadSectionFragment::getForwardSegment)
                .map(DirectionalSegment::getLineString)
                .toList();
    }

    public List<LineString> getBackwardGeometries() {

        if (!hasBackwardSegments()) {
            throw new IllegalStateException("no backward geometry found for road section %s".formatted(id));
        }

        return roadSectionFragments.stream()
                .map(RoadSectionFragment::getBackwardSegment)
                .map(DirectionalSegment::getLineString)
                .toList();
    }

}
