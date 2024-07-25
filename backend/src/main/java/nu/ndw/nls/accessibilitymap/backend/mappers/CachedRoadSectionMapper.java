package nu.ndw.nls.accessibilitymap.backend.mappers;

import nu.ndw.nls.accessibilitymap.backend.model.CachedRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
public class CachedRoadSectionMapper {

    private static final String ROAD_SECTION_DRIVING_DIRECTION_BACKWARD = "T";

    public CachedRoadSection map(NwbRoadSectionDto nwbRoadSectionDto) {
        final LineString geometry;

        if (ROAD_SECTION_DRIVING_DIRECTION_BACKWARD.equals(nwbRoadSectionDto.getDrivingDirection())) {
            geometry = nwbRoadSectionDto.getGeometry().reverse();
        } else {
            geometry = nwbRoadSectionDto.getGeometry();
        }

        return new CachedRoadSection((int) nwbRoadSectionDto.getRoadSectionId(), geometry);
    }

}
