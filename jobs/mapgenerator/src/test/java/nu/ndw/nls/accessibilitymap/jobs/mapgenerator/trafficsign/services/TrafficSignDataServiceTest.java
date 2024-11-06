package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.AnnotationUtil;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.mappers.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
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

    @BeforeEach
    void setUp() {

        trafficSignDataService = new TrafficSignDataService(trafficSignMapper, trafficSignService);
    }

    @Test
    void findAllByTypes_ok() {

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
