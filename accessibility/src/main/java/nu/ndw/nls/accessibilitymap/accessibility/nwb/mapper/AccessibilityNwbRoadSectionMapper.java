package nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityNwbRoadSectionMapper {

    private static final String ROAD_SECTION_DRIVING_DIRECTION_FORWARD = "H";

    private static final String ROAD_SECTION_DRIVING_DIRECTION_BACKWARD = "T";

    public AccessibilityNwbRoadSection map(NwbRoadSectionDto nwbRoadSectionDto) {
        // We only check H and T values, all other values mean accessible.
        boolean forwardAccessible =
                !ROAD_SECTION_DRIVING_DIRECTION_BACKWARD.equals(nwbRoadSectionDto.getDrivingDirection());
        boolean reverseAccessible =
                !ROAD_SECTION_DRIVING_DIRECTION_FORWARD.equals(nwbRoadSectionDto.getDrivingDirection());

        return new AccessibilityNwbRoadSection(
                nwbRoadSectionDto.getRoadSectionId(),
                nwbRoadSectionDto.getJunctionIdFrom(),
                nwbRoadSectionDto.getJunctionIdTo(),
                nwbRoadSectionDto.getMunicipalityId(),
                nwbRoadSectionDto.getGeometry(),
                forwardAccessible,
                reverseAccessible);
    }
}
