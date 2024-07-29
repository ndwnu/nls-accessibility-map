package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.backend.model.CachedRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
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

    public CachedRoadSection map(NwbRoadSectionDto nwbRoadSectionDto) {
        // We only check H and T values, all other values mean accessible.
        boolean forwardAccessible =
                !ROAD_SECTION_DRIVING_DIRECTION_BACKWARD.equals(nwbRoadSectionDto.getDrivingDirection());
        boolean reverseAccessible =
                !ROAD_SECTION_DRIVING_DIRECTION_FORWARD.equals(nwbRoadSectionDto.getDrivingDirection());

        return new CachedRoadSection((int) nwbRoadSectionDto.getRoadSectionId(), nwbRoadSectionDto.getGeometry(),
                forwardAccessible, reverseAccessible);
    }

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

        assertEquals(new CachedRoadSection((int) ID, geometry, true, false),
                cachedRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_ok_reverseOnly() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_BACKWARD);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        assertEquals(new CachedRoadSection((int) ID, geometry, false, true),
                cachedRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_ok_drivingDirectionBeideResultsInBothDirections() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_BEIDE);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        assertEquals(new CachedRoadSection((int) ID, geometry, true, true),
                cachedRoadSectionMapper.map(nwbRoadSectionDto));
    }

    @Test
    void map_ok_drivingDirectionOnbekendResultsInBothDirections() {
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(ID);

        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(ROAD_SECTION_DRIVING_DIRECTION_ONBEKEND);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        assertEquals(new CachedRoadSection((int) ID, geometry, true, true),
                cachedRoadSectionMapper.map(nwbRoadSectionDto));
    }
}