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
import nu.ndw.nls.geometry.stream.collectors.GeometryCollectors;
import org.locationtech.jts.geom.LineString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@Builder
@With
@Validated
public final class RoadSection {

    @NotNull
    private final Long id;

    private RoadSectionMetaData metaData;

    @Default
    @NotEmpty
    @Valid
    private List<RoadSectionFragment> roadSectionFragments = new ArrayList<>();

    public boolean isRestrictedInAnyDirection() {
        return roadSectionFragments.stream()
                .anyMatch(RoadSectionFragment::isNotAccessibleFromAllSegments) || roadSectionFragments.stream()
                .anyMatch(RoadSectionFragment::isPartiallyAccessible);
    }

    public Boolean isForwardAccessible() {
        return roadSectionFragments.stream()
                .map(RoadSectionFragment::isForwardAccessible)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

    }

    public Boolean isBackwardAccessible() {
        return roadSectionFragments.stream()
                .map(RoadSectionFragment::isBackwardAccessible)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public LineString getMergedForwardGeometry() {
        return roadSectionFragments.stream()
                .map(RoadSectionFragment::getForwardSegment)
                .map(DirectionalSegment::getLineString)
                .collect(GeometryCollectors.mergeToLineString())
                .orElseThrow(() -> new IllegalStateException("no forward geometry found"));
    }
}
