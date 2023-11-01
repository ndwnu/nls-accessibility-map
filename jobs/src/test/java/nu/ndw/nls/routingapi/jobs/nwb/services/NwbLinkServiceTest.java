package nu.ndw.nls.routingapi.jobs.nwb.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingapi.jobs.nwb.mappers.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbLinkServiceTest {

    private static final int VERSION_INT = 20220101;
    private static final long ROAD_SECTION_ID_1 = 1L;
    private static final long ROAD_SECTION_ID_2 = 2L;
    private static final long ROAD_SECTION_ID_3 = 3L;

    @Mock
    private NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;

    @Mock
    private NwbRoadSectionService roadSectionService;

    @InjectMocks
    private NwbLinkService nwbLinkService;

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

        // Spy on stream, so we can verify it gets closed
        Stream<NwbRoadSectionDto> roadSectionsStream = spy(roadSectionDtos.stream());

        when(roadSectionService.findLazyCar(VERSION_INT)).thenReturn(roadSectionsStream);

        when(nwbRoadSectionToLinkMapper.map(roadSection1)).thenReturn(roadSection1link);
        when(nwbRoadSectionToLinkMapper.map(roadSection2)).thenReturn(roadSection2link);
        when(nwbRoadSectionToLinkMapper.map(roadSection3)).thenReturn(roadSection3link);

        List<Link> result = nwbLinkService.getLinks(VERSION_INT);

        assertEquals(List.of(roadSection1link, roadSection2link, roadSection3link), result);

        // Verify stream closure
        verify(roadSectionsStream).close();
    }
}
