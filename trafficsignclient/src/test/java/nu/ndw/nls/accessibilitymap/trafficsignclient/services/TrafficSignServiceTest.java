package nu.ndw.nls.accessibilitymap.trafficsignclient.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignClientProperties;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonFeatureCollectionDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.repositories.TrafficSignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignServiceTest {

    private static final String RVV_CODE_B = "rvv-code-b";
    private static final String RVV_CODE_A = "rvv-code-a";

    @Mock
    private TrafficSignRepository trafficSignRepository;

    @Mock
    private TrafficSignClientProperties trafficSignProperties;

    @InjectMocks
    private TrafficSignService trafficSignService;

    @Mock
    private TrafficSignClientProperties.TrafficSignApiProperties api;

    @Test
    void getTrafficSigns_filteredAndGrouped() {

        when(trafficSignProperties.getApi()).thenReturn(api);

        TrafficSignPropertiesDto trafficSignPropertiesDto1 = TrafficSignPropertiesDto.builder()
                .build();
        TrafficSignGeoJsonDto trafficSign1 = TrafficSignGeoJsonDto.builder()
                .properties(trafficSignPropertiesDto1)
                .build();

        TrafficSignPropertiesDto trafficSignPropertiesDto2 = TrafficSignPropertiesDto.builder()
                .nwbVersion(LocalDate.of(2023, 11, 1))
                .build();
        TrafficSignGeoJsonDto trafficSign2 = TrafficSignGeoJsonDto.builder()
                .properties(trafficSignPropertiesDto2)
                .build();

        TrafficSignPropertiesDto trafficSignPropertiesDto3 = TrafficSignPropertiesDto.builder()
                .roadSectionId(2L)
                .nwbVersion(LocalDate.of(2023, 10, 1))
                .build();
        TrafficSignGeoJsonDto trafficSign3 = TrafficSignGeoJsonDto.builder()
                .properties(trafficSignPropertiesDto3)
                .build();

        TrafficSignPropertiesDto trafficSignPropertiesDto4 = TrafficSignPropertiesDto.builder()
                .roadSectionId(1L)
                .nwbVersion(LocalDate.of(2023, 9, 1))
                .build();
        TrafficSignGeoJsonDto trafficSign4 = TrafficSignGeoJsonDto.builder()
                .properties(trafficSignPropertiesDto4)
                .build();

        TrafficSignPropertiesDto trafficSignPropertiesDto5 = TrafficSignPropertiesDto.builder()
                .roadSectionId(1L)
                .nwbVersion(LocalDate.of(2023, 9, 1))
                .build();
        TrafficSignGeoJsonDto trafficSign5 = TrafficSignGeoJsonDto.builder()
                .properties(trafficSignPropertiesDto5)
                .build();

        TrafficSignPropertiesDto trafficSignPropertiesDto6 = TrafficSignPropertiesDto.builder()
                .roadSectionId(2L)
                .build();
        TrafficSignGeoJsonDto trafficSign6 = TrafficSignGeoJsonDto.builder()
                .properties(trafficSignPropertiesDto6)
                .build();

        when(trafficSignRepository.findCurrentState(
                CurrentStateStatus.PLACED,
                Set.of(RVV_CODE_A, RVV_CODE_B),
                null,
                null)
        ).thenReturn(TrafficSignGeoJsonFeatureCollectionDto.builder()
                .features(List.of(trafficSign1, trafficSign2, trafficSign3, trafficSign4, trafficSign5,
                        trafficSign6))
                .build());

        TrafficSignData result = trafficSignService.getTrafficSigns(
                Set.of(RVV_CODE_A, RVV_CODE_B),
                Collections.emptySet());

        Map<Long, List<TrafficSignGeoJsonDto>> longListMap = result.trafficSignsByRoadSectionId();

        assertTrue(longListMap.containsKey(1L));
        List<TrafficSignGeoJsonDto> trafficSignGeoJsonDtos = longListMap.get(1L);
        assertEquals(2, trafficSignGeoJsonDtos.size());
        assertTrue(trafficSignGeoJsonDtos.contains(trafficSign4));
        assertTrue(trafficSignGeoJsonDtos.contains(trafficSign5));

        assertTrue(longListMap.containsKey(2L));
        trafficSignGeoJsonDtos = longListMap.get(2L);
        assertEquals(2, trafficSignGeoJsonDtos.size());
        assertTrue(trafficSignGeoJsonDtos.contains(trafficSign3));
        assertTrue(trafficSignGeoJsonDtos.contains(trafficSign6));
        assertEquals(LocalDate.of(2023, 10, 1), result.maxNwbReferenceDate());
    }
}
