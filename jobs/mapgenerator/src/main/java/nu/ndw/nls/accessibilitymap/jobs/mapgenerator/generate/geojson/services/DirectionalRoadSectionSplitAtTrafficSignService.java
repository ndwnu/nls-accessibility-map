package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectionalRoadSectionSplitAtTrafficSignService {

    private static final int END_FRACTION = 1;
    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    /**
     * Splits one {@link DirectionalRoadSectionAndTrafficSign} into two copies if it has a traffic and marks the part
     * in front of the traffic sign as accessible and the part behind it as inaccessible
     *
     * @param roadSectionAndTrafficSign road section and traffic sign
     * @return the original or two {@link DirectionalRoadSectionAndTrafficSign}, depending on whether there was a
     *          traffic sign
     */
    public List<DirectionalRoadSectionAndTrafficSign> split(
            DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSign) {

        DirectionalTrafficSign directionalTrafficSign = roadSectionAndTrafficSign.getTrafficSign();
        if (directionalTrafficSign == null) {
            // No sign, no splitting
            return List.of(roadSectionAndTrafficSign);
        }

        DirectionalRoadSection directionalRoadSection = roadSectionAndTrafficSign.getRoadSection();
        LineString startGeometry = fractionAndDistanceCalculator.getSubLineString(
                directionalRoadSection.getNwbGeometry(), directionalTrafficSign.getNwbFraction());

        LineString endGeometry = fractionAndDistanceCalculator.getSubLineString(directionalRoadSection.getNwbGeometry(),
                directionalTrafficSign.getNwbFraction(), END_FRACTION);

        boolean startIsAccessible = isStartAccessibleInNwbDirection(directionalRoadSection);

        return List.of( createDirectionalRoadSectionPart(roadSectionAndTrafficSign, startGeometry, startIsAccessible),
                        createDirectionalRoadSectionPart(roadSectionAndTrafficSign, endGeometry, !startIsAccessible));
    }

    private boolean isStartAccessibleInNwbDirection(DirectionalRoadSection directionalRoadSection) {
        return directionalRoadSection.getDirection() == Direction.FORWARD;
    }

    private DirectionalRoadSectionAndTrafficSign createDirectionalRoadSectionPart(
            DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSign, LineString geometry, boolean accessible) {

        DirectionalRoadSection newRoadSection = roadSectionAndTrafficSign.getRoadSection()
                .withAccessible(accessible)
                .withNwbGeometry(geometry);

        return roadSectionAndTrafficSign.withRoadSection(newRoadSection);
    }

}
