package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

@ExtendWith(MockitoExtension.class)
class TrafficSignDataServiceTest {

    private TrafficSignDataService trafficSignDataService;

    @Mock
    private TrafficSignMapper trafficSignMapper;

    @Mock
    private TrafficSignService trafficSignService;

    @Mock
    private TrafficSignData trafficSignData;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto1;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto2;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto3;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto4;

    @Mock
    private TrafficSign trafficSign1;

    @Mock
    private TrafficSign trafficSign2;

    @Mock
    private TrafficSign trafficSign3;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        trafficSignDataService = new TrafficSignDataService(trafficSignMapper, trafficSignService);
    }

    @Test
    void findAllBy() {

        when(trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
                .map(TrafficSignType::getRvvCode)
                .collect(Collectors.toSet()))).thenReturn(trafficSignData);

        when(trafficSignData.trafficSignsByRoadSectionId()).thenReturn(Map.of(
                1L, List.of(trafficSignGeoJsonDto1, trafficSignGeoJsonDto2),
                2L, List.of(trafficSignGeoJsonDto3),
                3L, List.of(trafficSignGeoJsonDto4)
        ));

        mockMapperCalls(trafficSignGeoJsonDto1, trafficSign1);
        mockMapperCalls(trafficSignGeoJsonDto2, trafficSign2);
        mockMapperCalls(trafficSignGeoJsonDto3, trafficSign3);
        mockMapperCalls(trafficSignGeoJsonDto4, null);

        when(trafficSign1.isRelevant(accessibilityRequest)).thenReturn(true);
        when(trafficSign2.isRelevant(accessibilityRequest)).thenReturn(false);
        when(trafficSign3.isRelevant(accessibilityRequest)).thenReturn(true);

        List<TrafficSign> trafficSigns = trafficSignDataService.findAllBy(accessibilityRequest);

        assertThat(trafficSigns).containsExactlyInAnyOrder(trafficSign1, trafficSign3);
    }

    @Test
    void findAllByTypes() {

        when(trafficSignService.getTrafficSigns(Set.of("C7b"))).thenReturn(trafficSignData);
        when(trafficSignData.trafficSignsByRoadSectionId()).thenReturn(Map.of(
                1L, List.of(trafficSignGeoJsonDto1, trafficSignGeoJsonDto2),
                2L, List.of(trafficSignGeoJsonDto3),
                3L, List.of(trafficSignGeoJsonDto4)
        ));

        mockMapperCalls(trafficSignGeoJsonDto1, trafficSign1);
        mockMapperCalls(trafficSignGeoJsonDto2, trafficSign2);
        mockMapperCalls(trafficSignGeoJsonDto3, trafficSign3);
        mockMapperCalls(trafficSignGeoJsonDto4, null);

        List<TrafficSign> trafficSigns = trafficSignDataService.findAllByTypes(List.of(TrafficSignType.C7B));

        assertThat(trafficSigns).containsExactlyInAnyOrder(trafficSign1, trafficSign2, trafficSign3);
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                trafficSignDataService.getClass(),
                Service.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    private void mockMapperCalls(TrafficSignGeoJsonDto trafficSignGeoJsonDto, TrafficSign trafficSign) {

        when(trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                eq(trafficSignGeoJsonDto),
                any(IntegerSequenceSupplier.class))
        ).thenReturn(Optional.ofNullable(trafficSign));
    }
}
