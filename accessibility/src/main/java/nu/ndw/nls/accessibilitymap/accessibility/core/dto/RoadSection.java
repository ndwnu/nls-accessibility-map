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

    @Default
    @NotEmpty
    @Valid
    private List<RoadSectionFragment> roadSectionFragments = new ArrayList<>();

    public boolean isRestrictedInAnyDirection() {
        return roadSectionFragments.stream()
                .anyMatch(RoadSectionFragment::isNotAccessibleFromAllSegments) || roadSectionFragments.stream()
                .anyMatch(RoadSectionFragment::isPartiallyAccessible);
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

    /**
     * Evaluates the forward accessibility of the road section by examining all associated road section fragments. If any fragment
     * explicitly indicates restricted forward accessibility, the method returns false. If all fragments are forwardly accessible, the
     * method returns true.
     *
     * @return true if all fragments in the road section are forwardly accessible; false if any fragment is not forwardly accessible.
     */
    public boolean isForwardAccessible() {

        return roadSectionFragments.stream()
                .map(RoadSectionFragment::isForwardAccessible)
                .filter(accessible -> !accessible)
                .findFirst()
                .orElse(true);

    }

    /**
     * Determines if the road section is backward accessible by evaluating the backward accessibility of all associated road section
     * fragments. If any fragment explicitly indicates restricted backward accessibility, the method returns false. If all fragments are
     * backward accessible, the method returns true.
     *
     * @return true if all fragments in the road section are backward accessible; false if any fragment is not backward accessible.
     */
    public boolean isBackwardAccessible() {
        return roadSectionFragments.stream()
                .map(RoadSectionFragment::isBackwardAccessible)
                .filter(accessible -> !accessible)
                .findFirst()
                .orElse(true);


    }

    /**
     * Merges the forward geometry of all directional segments within the collection of road section fragments into a single LineString
     * representation.
     *
     * @return the merged LineString of all forward directional segments in the current road section
     * @throws IllegalStateException if no forward geometry can be found
     */
    public LineString getMergedForwardGeometry() {
        if (!hasForwardSegments()) {
            throw new IllegalStateException("no forward geometry found for road section " + id);
        }
        return roadSectionFragments.stream()
                .map(RoadSectionFragment::getForwardSegment)
                .map(DirectionalSegment::getLineString)
                .collect(GeometryCollectors.mergeToLineString())
                .orElseThrow(() -> new IllegalStateException("no forward geometry found for road section " + id));
    }


    /**
     * Merges the backward geometry of all directional segments within the collection of road section fragments into a single LineString
     * representation.
     *
     * @return the merged LineString of all backward directional segments in the current road section
     * @throws IllegalStateException if no backward geometry can be found
     */
    public LineString getMergedBackWardGeometry() {
        if (!hasBackwardSegments()) {
            throw new IllegalStateException("no backward geometry found for road section " + id);
        }
        return roadSectionFragments.stream()
                .map(RoadSectionFragment::getBackwardSegment)
                .map(DirectionalSegment::getLineString)
                .collect(GeometryCollectors.mergeToLineString())
                .orElseThrow(() -> new IllegalStateException("no backward geometry found for road section " + id));
    }
}
