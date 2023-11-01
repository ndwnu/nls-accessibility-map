package nu.ndw.nls.routingapi.jobs.nwb.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbRoadSectionToLinkMapperTest {

    private static final double GEOMETRY_LENGTH = 10D;
    private static final Long JUNCTION_ID_FROM = 1L;
    private static final Long JUNCTION_ID_TO = 2L;
    private static final double SPEED_FORWARD_KMH = 50D;
    private static final double SPEED_REVERSE_KMH = 0D;
    private static final int ROAD_SECTION_ID = 3;
    private static final String FORWARD = "H";

    @Mock
    private RijksdriehoekToWgs84Mapper rijksdriehoekToWgs84Mapper;

    @InjectMocks
    private NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;

    @Mock
    private LineString lineString;

    @Mock
    private LineString lineStringRijksdriehoek;

    @BeforeEach
    void setUp() {
        when(lineString.getLength()).thenReturn(GEOMETRY_LENGTH);
        when(rijksdriehoekToWgs84Mapper.map(lineString)).thenReturn(lineStringRijksdriehoek);
    }

    @Test
    void map_ok() {
        Link link = nwbRoadSectionToLinkMapper.map(NwbRoadSectionDto.builder()
                        .roadSectionId(ROAD_SECTION_ID)
                        .junctionIdFrom(JUNCTION_ID_FROM)
                        .junctionIdTo(JUNCTION_ID_TO)
                        .drivingDirection(FORWARD)
                        .geometry(lineString)
                        .build());

        assertEquals(ROAD_SECTION_ID, link.getId());
        assertEquals(JUNCTION_ID_FROM, link.getFromNodeId());
        assertEquals(JUNCTION_ID_TO, link.getToNodeId());
        assertEquals(SPEED_FORWARD_KMH, link.getSpeedInKilometersPerHour());
        assertEquals(SPEED_REVERSE_KMH, link.getReverseSpeedInKilometersPerHour());
        assertEquals(GEOMETRY_LENGTH, link.getDistanceInMeters());
        assertEquals(lineStringRijksdriehoek, link.getGeometry());
    }
}
