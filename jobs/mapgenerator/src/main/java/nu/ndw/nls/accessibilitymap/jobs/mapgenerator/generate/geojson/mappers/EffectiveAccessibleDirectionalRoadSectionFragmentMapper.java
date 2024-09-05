package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import org.springframework.stereotype.Component;

@Component
public class EffectiveAccessibleDirectionalRoadSectionFragmentMapper {

    private static final double FRACTION_START_0 = 0;
    private static final double FRACTION_END_1 = 1;

    /**
     * Splits the road section at the into two fractions, before and after the traffic sign.
     *
     * @todo: this mapper makes an assumption that the road in front of a traffic sign is accessible and the road
     *        behind the traffic sign is not. We cannot accurately state that it is, because there might be a
     *        previous road section that is also inaccessible effectively making the part in front of the traffic
     *        sign inaccessible.
     * @param rst
     * @return
     */
    public List<RoadSectionFragment<EffectiveAccessibleDirectionalRoadSection>> map(
            DirectionalRoadSectionAndTrafficSign rst) {

        if (rst == null) {
            return Collections.emptyList();
        }

        if (rst.getTrafficSign() == null) {
            // When there is no traffic sign, it is only accessible if the accessibility result explicitly said it was
            // accessible. It could be null, meaning it was not accessible in the traffic sign unrestricted isochrone
            return List.of(RoadSectionFragment.<EffectiveAccessibleDirectionalRoadSection>builder()
                    .data(EffectiveAccessibleDirectionalRoadSection.builder()
                            .accessibility(rst.getRoadSection().getAccessible())
                            .roadSection(rst)
                            .build())
                    .fromFraction(FRACTION_START_0)
                    .toFraction(FRACTION_END_1)
                    .build());
        }

        double trafficSignNwbFraction = rst.getTrafficSign().getNwbFraction();

        // As stated in the description above, it is assumed that the traffic sign is splitting the road section in
        // an accessible part in front of the traffic sign (in driving direction) and inacessible after the
        // driving direction. As for now, checking the 'accessible' state of the road section is not required,
        // because a traffic sign on this road section automatically means it is partly inaccessible
        boolean firstPartAccessibleInNWBGeometryDirection = isFirstPartAccessibleInDrivingDirection(rst);

        return List.of(
                RoadSectionFragment.<EffectiveAccessibleDirectionalRoadSection>builder()
                        .fromFraction(FRACTION_START_0)
                        .toFraction(trafficSignNwbFraction)
                        .data(EffectiveAccessibleDirectionalRoadSection.builder()
                                .accessibility(firstPartAccessibleInNWBGeometryDirection)
                                .roadSection(rst)
                                .build())
                        .build(),
                RoadSectionFragment.<EffectiveAccessibleDirectionalRoadSection>builder()
                        .fromFraction(trafficSignNwbFraction)
                        .toFraction(FRACTION_END_1)
                        .data(EffectiveAccessibleDirectionalRoadSection.builder()
                                .accessibility(!firstPartAccessibleInNWBGeometryDirection)
                                .roadSection(rst)
                                .build())
                        .build());
    }

    /**
     * In driving direction, the first part is accessible and the part after the road sign is not. But for backwards
     * we need to reverse this logic, because we are operating on the original NWB geometry direction.
     * @param direction the road section and traffic sign
     * @return true if first part in NWB geometry direction is accessible
     */
    private boolean isFirstPartAccessibleInDrivingDirection(DirectionalRoadSectionAndTrafficSign direction) {
        return direction.getRoadSection().getDirection() == Direction.FORWARD;
    }

}
