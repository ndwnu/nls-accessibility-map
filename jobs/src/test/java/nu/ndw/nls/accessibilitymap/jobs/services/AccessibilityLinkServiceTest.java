package nu.ndw.nls.accessibilitymap.jobs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.jobs.nwb.mappers.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.accessibilitymap.jobs.nwb.services.NwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers.TrafficSignMapperRegistry;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.LocationJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.RoadJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbVersionDto;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityLinkServiceTest {

    private static final long ROAD_SECTION_ID_1 = 1L;
    private static final long ROAD_SECTION_ID_2 = 2L;
    private static final long ROAD_SECTION_ID_3 = 3L;
    private static final long ROAD_SECTION_ID_4 = 4L;
    private static final LocalDate MAX_NWB_REFERENCE_DATE = LocalDate.of(2023, 10, 1);
    private static final Instant MAX_EVENT_TIMESTAMP = Instant.parse("2023-11-07T15:37:23Z");
    private static final int NWB_VERSION_ID = 20231001;

    @Mock
    private TrafficSignService trafficSignService;
    @Mock
    private NwbVersionCrudService nwbVersionService;
    @Mock
    private NwbRoadSectionService nwbRoadSectionService;
    @Mock
    private NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;

    @InjectMocks
    private AccessibilityLinkService accessibilityLinkService;

    @Mock
    private AccessibilityLink roadSection1link;

    @Mock
    private AccessibilityLink roadSection2link;

    @Mock
    private AccessibilityLink roadSection3link;

    @Mock
    private TrafficSignMapperRegistry trafficSignMapperRegistry;

    @Mock
    private java.util.Set<String> rvvCodesSet;

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
        List<NwbRoadSectionDto> roadSections = List.of(roadSection1, roadSection2, roadSection3);

        TrafficSignJsonDtoV3 trafficSign1 = createTrafficSign(ROAD_SECTION_ID_1);
        TrafficSignJsonDtoV3 trafficSign2 = createTrafficSign(ROAD_SECTION_ID_1);
        TrafficSignJsonDtoV3 trafficSign3 = createTrafficSign(ROAD_SECTION_ID_3);
        TrafficSignJsonDtoV3 trafficSign4 = createTrafficSign(ROAD_SECTION_ID_4);
        Map<Long, List<TrafficSignJsonDtoV3>> trafficSignsByRoadSectionId = Map.of(
                ROAD_SECTION_ID_1, List.of(trafficSign1, trafficSign2),
                ROAD_SECTION_ID_3, List.of(trafficSign3),
                ROAD_SECTION_ID_4, List.of(trafficSign4));

        // Spy on stream, so we can verify it gets closed
        Stream<NwbRoadSectionDto> roadSectionStream = spy(roadSections.stream());
        when(trafficSignMapperRegistry.getIncludedRvvCodes()).thenReturn(rvvCodesSet);

        when(trafficSignService.getTrafficSigns(rvvCodesSet)).thenReturn(
                new TrafficSignData(trafficSignsByRoadSectionId, MAX_NWB_REFERENCE_DATE, MAX_EVENT_TIMESTAMP));
        when(nwbVersionService.findLatestByReferenceDate(MAX_NWB_REFERENCE_DATE)).thenReturn(
                Optional.of(NwbVersionDto.builder().versionId(NWB_VERSION_ID).build()));
        when(nwbRoadSectionService.findLazyCar(NWB_VERSION_ID)).thenReturn(roadSectionStream);

        when(nwbRoadSectionToLinkMapper.map(roadSection1, List.of(trafficSign1, trafficSign2))).thenReturn(roadSection1link);
        when(nwbRoadSectionToLinkMapper.map(roadSection2, Collections.emptyList())).thenReturn(roadSection2link);
        when(nwbRoadSectionToLinkMapper.map(roadSection3, List.of(trafficSign3))).thenReturn(roadSection3link);

        AccessibilityLinkData result = accessibilityLinkService.getLinks();

        assertEquals(List.of(roadSection1link, roadSection2link, roadSection3link), result.links());
        assertEquals(NWB_VERSION_ID, result.nwbVersionId());
        assertEquals(MAX_EVENT_TIMESTAMP, result.trafficSignTimestamp());

        // Verify stream closure
        verify(roadSectionStream).close();
    }

    private TrafficSignJsonDtoV3 createTrafficSign(long roadSectionId2) {
        return TrafficSignJsonDtoV3.builder()
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId(String.valueOf(roadSectionId2))
                                .build())
                        .build())
                .build();
    }
}
