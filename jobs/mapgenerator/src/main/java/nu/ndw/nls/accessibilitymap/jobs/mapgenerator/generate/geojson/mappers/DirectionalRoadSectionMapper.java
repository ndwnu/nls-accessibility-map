package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.ArrayList;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import org.locationtech.jts.geom.LineString;
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

        if (isAccessiblePriorRestrictions(roadSection, false)) {
            directionalRoadSections.add(DirectionalRoadSection.builder()
                    .nwbRoadSectionId(roadSection.getRoadSectionId())
                    .roadSectionId(roadSection.getRoadSectionId())
                    .geometry(roadSection.getGeometry())
                    .accessible(roadSection.getForwardAccessible())
                    .build());
        }

        if (isAccessiblePriorRestrictions(roadSection, true)) {
            directionalRoadSections.add(DirectionalRoadSection.builder()
                    .nwbRoadSectionId(roadSection.getRoadSectionId())
                    .roadSectionId(negateIdForReverseRoadSection(roadSection.getRoadSectionId()))
                    .geometry(reverseGeometryForReversedRoadSection(roadSection.getGeometry()))
                    .accessible(roadSection.getBackwardAccessible())
                    .build());
        }

        return directionalRoadSections;
    }

    private LineString reverseGeometryForReversedRoadSection(LineString geometry) {
        return geometry.reverse();
    }

    /**
     * To have unique id's for forward and reverse direction response, we negate the id of the reverse direction
     * @param id the id for a reverse direction road section
     * @return negated id
     */
    private long negateIdForReverseRoadSection(long id) {
        return -id;
    }

    /**
     *
     * @param roadSection the road section
     * @param reverse reverse
     * @return true if it has a non-null value
     */
    private boolean isAccessiblePriorRestrictions(RoadSection roadSection, boolean reverse) {
        return getAccessibleForDirection(roadSection, reverse) != null;
    }

    private Boolean getAccessibleForDirection(RoadSection roadSection, boolean reverse) {
        return reverse ? roadSection.getBackwardAccessible() : roadSection.getForwardAccessible();
    }

}
