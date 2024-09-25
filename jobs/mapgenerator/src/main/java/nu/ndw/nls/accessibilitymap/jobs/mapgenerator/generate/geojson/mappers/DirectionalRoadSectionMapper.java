package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.ArrayList;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import org.springframework.stereotype.Component;

@Component
public class DirectionalRoadSectionMapper {

    /**
     * Maps {@link RoadSection} responses to {@link DirectionalRoadSection}, which is a road section for a certain
     * direction. Simplifies further processing by removing logic for directionality. Will return zero, one or two
     * {@link DirectionalRoadSection} objects, depending on whether the direction booleans are non-null, which indicate
     * that they are accessible prior restrictions. Will negate the id and reverse the geometry for
     * reverse road sections.
     *
     *
     * @param roadSection road section
     * @return zero, one or two {@link DirectionalRoadSection}
     */
    public List<DirectionalRoadSection> map(RoadSection roadSection) {
        List<DirectionalRoadSection> directionalRoadSections = new ArrayList<>();

        if (isAccessiblePriorRestrictions(roadSection, Direction.BACKWARD)) {
            directionalRoadSections.add(DirectionalRoadSection.builder()
                    .direction(Direction.BACKWARD)
                    .nwbRoadSectionId(roadSection.getRoadSectionId())
                    .nwbGeometry(roadSection.getGeometry())
                    .accessible(roadSection.getBackwardAccessible())
                    .build());
        }

        if (isAccessiblePriorRestrictions(roadSection, Direction.FORWARD)) {
            directionalRoadSections.add(DirectionalRoadSection.builder()
                            .direction(Direction.FORWARD)
                            .nwbRoadSectionId(roadSection.getRoadSectionId())
                            .nwbGeometry(roadSection.getGeometry())
                            .accessible(roadSection.getForwardAccessible())
                            .build());
        }

        return directionalRoadSections;
    }

    /**
     *
     * @param roadSection the road section
     * @param direction direction
     * @return true if it has a non-null value
     */
    private boolean isAccessiblePriorRestrictions(RoadSection roadSection, Direction direction) {
        return getAccessibleForDirection(roadSection, direction) != null;
    }

    private Boolean getAccessibleForDirection(RoadSection roadSection, Direction direction) {
        if (direction == Direction.FORWARD) {
            return roadSection.getForwardAccessible();
        } else {
            return roadSection.getBackwardAccessible();
        }
    }

}
