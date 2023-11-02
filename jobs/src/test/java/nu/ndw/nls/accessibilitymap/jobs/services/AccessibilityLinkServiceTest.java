package nu.ndw.nls.accessibilitymap.jobs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.jobs.nwb.mappers.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.accessibilitymap.jobs.nwb.services.NwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services.TrafficSignService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityLinkServiceTest {

    private static final int VERSION_INT = 20220101;
    private static final long ROAD_SECTION_ID_1 = 1L;
    private static final long ROAD_SECTION_ID_2 = 2L;
    private static final long ROAD_SECTION_ID_3 = 3L;

    @Mock
    private NwbRoadSectionService roadSectionService;
    @Mock
    private TrafficSignService trafficSignService;
    @Mock
    private NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;

    @InjectMocks
    private AccessibilityLinkService accessibilityLinkService;

    @Mock
    private Link roadSection1link;

    @Mock
    private Link roadSection2link;

    @Mock
    private Link roadSection3link;

    @Test
    void getLinks_ok() {
        NwbRoadSectionDto roadSection1 = NwbRoadSectionDto.builder()
                .roadSectionId(ROAD_SECTION_ID_1)
                .build();
        NwbRoadSectionDto roadSection2 = NwbRoadSectionDto.builder()
                .roadSectionId(ROAD_SECTION_ID_2)
                .build();
        NwbRoadSectionDto roadSection3 = NwbRoadSectionDto.builder()
                .roadSectionId(ROAD_SECTION_ID_3)
                .build();
        List<NwbRoadSectionDto> roadSectionDtos = List.of(roadSection1, roadSection2, roadSection3);

        // Spy on streams, so we can verify they get closed
        Stream<NwbRoadSectionDto> roadSectionsStream = spy(roadSectionDtos.stream());
        Stream<TrafficSignJsonDtoV3> trafficSignStream = spy(Stream.of());

        when(roadSectionService.findLazyCar(VERSION_INT)).thenReturn(roadSectionsStream);
        when(trafficSignService.getTrafficSigns()).thenReturn(trafficSignStream);

        when(nwbRoadSectionToLinkMapper.map(roadSection1)).thenReturn(roadSection1link);
        when(nwbRoadSectionToLinkMapper.map(roadSection2)).thenReturn(roadSection2link);
        when(nwbRoadSectionToLinkMapper.map(roadSection3)).thenReturn(roadSection3link);

        List<Link> result = accessibilityLinkService.getLinks(VERSION_INT);

        assertEquals(List.of(roadSection1link, roadSection2link, roadSection3link), result);

        // Verify stream closure
        verify(roadSectionsStream, atLeastOnce()).close();
        verify(trafficSignStream, atLeastOnce()).close();
    }
}
