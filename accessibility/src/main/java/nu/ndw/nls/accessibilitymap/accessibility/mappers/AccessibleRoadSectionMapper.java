package nu.ndw.nls.accessibilitymap.accessibility.mappers;

import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.springframework.stereotype.Component;

@Component
public class AccessibleRoadSectionMapper {

    private static final String ROAD_SECTION_DRIVING_DIRECTION_FORWARD = "H";
    private static final String ROAD_SECTION_DRIVING_DIRECTION_BACKWARD = "T";

    public AccessibilityRoadSection map(NwbRoadSectionDto nwbRoadSectionDto) {
        // We only check H and T values, all other values mean accessible.
        boolean forwardAccessible =
                !ROAD_SECTION_DRIVING_DIRECTION_BACKWARD.equals(nwbRoadSectionDto.getDrivingDirection());
        boolean reverseAccessible =
                !ROAD_SECTION_DRIVING_DIRECTION_FORWARD.equals(nwbRoadSectionDto.getDrivingDirection());

        return new AccessibilityRoadSection((int) nwbRoadSectionDto.getRoadSectionId(), nwbRoadSectionDto.getGeometry(),
                forwardAccessible, reverseAccessible);
    }

}
