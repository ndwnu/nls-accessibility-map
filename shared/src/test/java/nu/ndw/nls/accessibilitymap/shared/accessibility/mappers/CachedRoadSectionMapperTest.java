package nu.ndw.nls.accessibilitymap.shared.accessibility.mappers;

import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.shared.accessibility.model.CachedRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachedRoadSectionMapperTest {

    private static final String ROAD_SECTION_DRIVING_DIRECTION_FORWARD = "H";
    private static final String ROAD_SECTION_DRIVING_DIRECTION_BACKWARD = "T";
    private static final String ROAD_SECTION_DRIVING_DIRECTION_BEIDE = "B";
    private static final String ROAD_SECTION_DRIVING_DIRECTION_ONBEKEND = "O";

    private static final long ID = 1234L;

    @InjectMocks
    private CachedRoadSectionMapper cachedRoadSectionMapper;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto;

    @Mock
    private LineString geometry;


    @Test
    void map_ok_forwardOnly() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_FORWARD);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        Assertions.assertEquals(new CachedRoadSection((int) ID, geometry, true, false),
                cachedRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_ok_reverseOnly() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_BACKWARD);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        Assertions.assertEquals(new CachedRoadSection((int) ID, geometry, false, true),
                cachedRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_ok_drivingDirectionBeideResultsInBothDirections() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_BEIDE);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        Assertions.assertEquals(new CachedRoadSection((int) ID, geometry, true, true),
                cachedRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_ok_drivingDirectionOnbekendResultsInBothDirections() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_ONBEKEND);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        Assertions.assertEquals(new CachedRoadSection((int) ID, geometry, true, true),
                cachedRoadSectionMapper.map(nwbRoadSectionDto));
    }
}