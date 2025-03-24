package nu.ndw.nls.accessibilitymap.accessibility.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRoadSectionMapperTest {

    private static final String ROAD_SECTION_DRIVING_DIRECTION_FORWARD = "H";
    private static final String ROAD_SECTION_DRIVING_DIRECTION_BACKWARD = "T";
    private static final String ROAD_SECTION_DRIVING_DIRECTION_BEIDE = "B";
    private static final String ROAD_SECTION_DRIVING_DIRECTION_ONBEKEND = "O";

    private static final long ID = 1234L;

    @InjectMocks
    private AccessibleRoadSectionMapper accessibleRoadSectionMapper;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto;

    @Mock
    private LineString geometry;


    @Test
    void map_forwardOnly() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_FORWARD);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        assertEquals(new AccessibilityRoadSection((int) ID, geometry, true, false),
                accessibleRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_reverseOnly() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_BACKWARD);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        assertEquals(new AccessibilityRoadSection((int) ID, geometry, false, true),
                accessibleRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_drivingDirectionBeideResultsInBothDirections() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_BEIDE);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        assertEquals(new AccessibilityRoadSection((int) ID, geometry, true, true),
                accessibleRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_drivingDirectionOnbekendResultsInBothDirections() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_ONBEKEND);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        assertEquals(new AccessibilityRoadSection((int) ID, geometry, true, true),
                accessibleRoadSectionMapper.map(nwbRoadSectionDto));
    }
}