package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.LocationJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.RoadJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.TrafficSignToDtoMapper;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.repositories.TrafficSignRepository;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignServiceTest {

    private static final String RVV_CODE_B = "rvv-code-b";
    private static final String RVV_CODE_A = "rvv-code-a";
    private TrafficSignService trafficSignService;
    @Mock
    private TrafficSignRepository trafficSignRepository;
    @Mock
    private TrafficSignToDtoMapper trafficSignToDtoMapper;


    @BeforeEach
    void setUp() {
        trafficSignService = new TrafficSignService(trafficSignRepository, trafficSignToDtoMapper);
    }

    @Test
    void getTrafficSigns_ok_filteredAndGrouped() {
        TrafficSignJsonDtoV3 trafficSign1 = TrafficSignJsonDtoV3.builder()
                .publicationTimestamp(Instant.parse("2023-11-07T15:37:23Z"))
                .location(LocationJsonDtoV3.builder()
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign2 = TrafficSignJsonDtoV3.builder()
                .publicationTimestamp(Instant.parse("2023-11-07T15:36:23Z"))
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .nwbVersion("2023-11-01")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign3 = TrafficSignJsonDtoV3.builder()
                .publicationTimestamp(Instant.parse("2023-11-07T15:35:23Z"))
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("2")
                                .nwbVersion("2023-10-01")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign4 = TrafficSignJsonDtoV3.builder()
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("1")
                                .nwbVersion("2023-09-01")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign5 = TrafficSignJsonDtoV3.builder()
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("1")
                                .nwbVersion("20231101")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign6 = TrafficSignJsonDtoV3.builder()
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("2")
                                .build())
                        .build())
                .build();

        when(trafficSignToDtoMapper.getRvvCodesUsed()).thenReturn(Set.of(RVV_CODE_A, RVV_CODE_B));

        when(trafficSignRepository.findCurrentState(CurrentStateStatus.PLACED, Set.of(RVV_CODE_A,RVV_CODE_B)))
                .thenReturn(Stream.of(trafficSign1, trafficSign2, trafficSign3,trafficSign4, trafficSign5, trafficSign6));

        TrafficSignData result = trafficSignService.getTrafficSigns();

        Map<Long, List<TrafficSignJsonDtoV3>> longListMap = result.trafficSignsByRoadSectionId();

        assertTrue(longListMap.containsKey(1L));
        List<TrafficSignJsonDtoV3> trafficSignJsonDtoV3s = longListMap.get(1L);
        assertEquals(2, trafficSignJsonDtoV3s.size());
        assertTrue(trafficSignJsonDtoV3s.contains(trafficSign4));
        assertTrue(trafficSignJsonDtoV3s.contains(trafficSign5));

        assertTrue(longListMap.containsKey(2L));
        trafficSignJsonDtoV3s = longListMap.get(2L);
        assertEquals(2, trafficSignJsonDtoV3s.size());
        assertTrue(trafficSignJsonDtoV3s.contains(trafficSign3));
        assertTrue(trafficSignJsonDtoV3s.contains(trafficSign6));

        assertEquals(Instant.parse("2023-11-07T15:37:23Z"), result.maxEventTimestamp());
        assertEquals(LocalDate.of(2023, 10, 1), result.maxNwbReferenceDate());
    }

}
