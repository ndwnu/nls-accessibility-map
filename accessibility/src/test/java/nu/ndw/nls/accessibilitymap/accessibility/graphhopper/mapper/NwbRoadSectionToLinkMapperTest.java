package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbRoadSectionToLinkMapperTest {

    private static final double GEOMETRY_LENGTH = 10;

    private static final int ROAD_SECTION_ID = 3;

    private static final long JUNCTION_ID_FROM = 1;

    private static final long JUNCTION_ID_TO = 2;

    private static final String DRIVING_DIRECTION_FORWARD = "H";

    private static final String DRIVING_DIRECTION_BACKWARD = "T";

    private static final String DRIVING_DIRECTION_BOTH = "B";

    private static final int MUNICIPALITY_ID = 307;

    @InjectMocks
    private NwbRoadSectionToLinkMapperImpl nwbRoadSectionToLinkMapper;

    @Mock
    private LineString lineString;

    @BeforeEach
    void setUp() {
        when(lineString.getLength()).thenReturn(GEOMETRY_LENGTH);
    }

    @Test
    void map() {
        AccessibilityLink link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_FORWARD));

        assertEquals(ROAD_SECTION_ID, link.getId());
        assertEquals(JUNCTION_ID_FROM, link.getFromNodeId());
        assertEquals(JUNCTION_ID_TO, link.getToNodeId());
        assertTrue(link.getAccessibility().forward());
        assertFalse(link.getAccessibility().reverse());
        assertEquals(GEOMETRY_LENGTH, link.getDistanceInMeters());
        assertEquals(lineString, link.getGeometry());
        assertEquals(307, link.getMunicipalityCode());
    }

    @Test
    void map_drivingDirectionBackward() {
        AccessibilityLink link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_BACKWARD));

        assertFalse(link.getAccessibility().forward());
        assertTrue(link.getAccessibility().reverse());
    }

    @Test
    void map_drivingDirectionBoth() {
        AccessibilityLink link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_BOTH));

        assertTrue(link.getAccessibility().forward());
        assertTrue(link.getAccessibility().reverse());
    }

    private NwbRoadSectionDto createRoadSectionDto(String drivingDirection) {
        return NwbRoadSectionDto.builder()
                .roadSectionId(ROAD_SECTION_ID)
                .junctionIdFrom(JUNCTION_ID_FROM)
                .junctionIdTo(JUNCTION_ID_TO)
                .drivingDirection(drivingDirection)
                .municipalityId(MUNICIPALITY_ID)
                .geometry(lineString)
                .build();
    }
}
