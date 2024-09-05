package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSections;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoadSectionFragmentSplitService {

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    /**
     * Takes the geometry from the first non-null direction and cuts it into the from and to fractions.
     *
     * @param fragment fragment of effective accessible directional road sections of which at least one direction
     *                 available
     * @return a list of directional road sections
     */
    public List<DirectionalRoadSectionAndTrafficSign> split(
            RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections> fragment) {

        EffectiveAccessibleDirectionalRoadSections data = fragment.getData();

        DirectionalRoadSectionAndTrafficSign backward = data.getBackward();
        DirectionalRoadSectionAndTrafficSign forward = data.getForward();

        // NWB geometry is the same for forward and backward
        LineString geometry;
        if (backward != null) {
            geometry = backward.getRoadSection().getNwbGeometry();
        } else if (forward != null) {
            geometry = forward.getRoadSection().getNwbGeometry();
        } else {
            throw new IllegalArgumentException("At least one of forward or backward must be provided");
        }

        LineString nwbGeometry = fractionAndDistanceCalculator.getSubLineString(geometry, fragment.getFromFraction(),
                fragment.getToFraction());

        return Stream.of(backward, forward)
                .filter(Objects::nonNull)
                .map(direction -> direction.withRoadSection(direction.getRoadSection()
                                                                .withNwbGeometry(nwbGeometry)
                                                                .withAccessible(data.getAccessibility())))
                .toList();
    }

}
