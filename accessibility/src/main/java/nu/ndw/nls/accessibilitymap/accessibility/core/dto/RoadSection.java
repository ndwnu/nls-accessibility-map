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

    /**
     * Evaluates the forward accessibility of the road section by examining all associated road section fragments. If any fragment
     * explicitly indicates restricted forward accessibility, the method returns false. If all fragments are forwardly accessible, the
     * method returns true.
     *
     * @return true if all fragments in the road section are forwardly accessible; false if any fragment is not forwardly accessible.
     */
    public Boolean isForwardAccessible() {

        return roadSectionFragments.stream()
                .map(RoadSectionFragment::isForwardAccessible)
                .filter(accessible -> !accessible)
                .findFirst()
                .orElse(true);

    }

    /**
     * Evaluates the backward accessibility of the road section by examining all associated road section fragments.
     * <p>
     * If any fragment explicitly indicates restricted or unknown backward accessibility, the method returns null. If all fragments are
     * backwardly accessible, the method returns true. If at least one fragment is not backwardly accessible, the method returns false.
     *
     * @return true if all fragments in the road section are backwardly accessible; false if at least one fragment is not backwardly
     * accessible; null if any fragment indicates unknown backward accessibility.
     */
    public Boolean isBackwardAccessible() {
        if (roadSectionFragments.stream()
                .map(RoadSectionFragment::isBackwardAccessible)
                .anyMatch(Objects::isNull)) {
            return null;
        } else {
            return roadSectionFragments.stream()
                    .map(RoadSectionFragment::isBackwardAccessible)
                    .filter(Objects::nonNull)
                    .filter(accessible -> !accessible)
                    .findFirst()
                    .orElse(true);


        }
    }

    /**
     * Merges the forward geometry of all directional segments within the collection of road section fragments into a single LineString
     * representation.
     *
     * @return the merged LineString of all forward directional segments in the current road section
     * @throws IllegalStateException if no forward geometry can be found
     */
    public LineString getMergedForwardGeometry() {
        return roadSectionFragments.stream()
                .map(RoadSectionFragment::getForwardSegment)
                .map(DirectionalSegment::getLineString)
                .collect(GeometryCollectors.mergeToLineString())
                .orElseThrow(() -> new IllegalStateException("no forward geometry found"));
    }
}
