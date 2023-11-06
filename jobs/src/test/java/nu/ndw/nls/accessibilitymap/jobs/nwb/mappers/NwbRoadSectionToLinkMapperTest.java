package nu.ndw.nls.accessibilitymap.jobs.nwb.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import nu.ndw.nls.routingmapmatcher.domain.model.LinkTag;
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
    private static final double DEFAULT_SPEED_KMH = 50;
    private static final double NO_ACCESS_SPEED_KMH = 0;

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
        Link link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_FORWARD));

        assertEquals(ROAD_SECTION_ID, link.getId());
        assertEquals(JUNCTION_ID_FROM, link.getFromNodeId());
        assertEquals(JUNCTION_ID_TO, link.getToNodeId());
        assertEquals(DEFAULT_SPEED_KMH, link.getSpeedInKilometersPerHour());
        assertEquals(NO_ACCESS_SPEED_KMH, link.getReverseSpeedInKilometersPerHour());
        assertEquals(GEOMETRY_LENGTH, link.getDistanceInMeters());
        assertEquals(lineStringRijksdriehoek, link.getGeometry());
        assertEquals(Map.of(LinkTag.MUNICIPALITY_CODE.getLabel(), 307), link.getTags());
    }

    @Test
    void map_ok_drivingDirectionBackward() {
        Link link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_BACKWARD));

        assertEquals(NO_ACCESS_SPEED_KMH, link.getSpeedInKilometersPerHour());
        assertEquals(DEFAULT_SPEED_KMH, link.getReverseSpeedInKilometersPerHour());
    }

    @Test
    void map_ok_drivingDirectionBoth() {
        Link link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_BOTH));

        assertEquals(DEFAULT_SPEED_KMH, link.getSpeedInKilometersPerHour());
        assertEquals(DEFAULT_SPEED_KMH, link.getReverseSpeedInKilometersPerHour());
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
