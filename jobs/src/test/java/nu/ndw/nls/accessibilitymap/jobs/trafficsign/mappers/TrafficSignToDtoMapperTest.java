package nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers.TrafficSignToDtoMapper.TrafficSignIncludedFilterPredicate;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers.signmappers.SignMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignToDtoMapperTest {

    private static final String RVV_CODE_A = "rvv_code_a";
    private static final String RVV_CODE_B = "rvv_code_b";
    @Spy
    private TrafficSignIncludedFilterPredicate predicateA;

    @Spy
    private TrafficSignIncludedFilterPredicate predicateB;

    @Mock
    TrafficSignMapperRegistry trafficSignMapperRegistry;

    private TrafficSignToDtoMapper trafficSignToDtoMapper;

    @Mock
    private TrafficSignJsonDtoV3 signA;

    @Mock
    private TrafficSignJsonDtoV3 signB;


    @Mock
    private SignMapper<?> signMapperA;

    @Mock
    private SignMapper<?> signMapperB;

    @Captor
    private ArgumentCaptor<TrafficSignAccessibilityDto> resultCaptor;

    private List<TrafficSignJsonDtoV3> trafficSigns;


    @Test
    void test_ok_allPredicatesTrueAndTrafficSignIsMappedBySingleMapper() {

        trafficSigns = List.of(signA);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB));

        when(signA.getRvvCode()).thenReturn(RVV_CODE_A);

        when(predicateA.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signA)).thenReturn(Boolean.TRUE);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Map.of(RVV_CODE_A, List.of(signA))));

        assertEquals(result, resultCaptor.getValue());
    }

    @Test
    void test_ok_allPredicatesTrueAndTrafficSignIsMappedByMultipleMappers() {

        trafficSigns = List.of(signA);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA, signMapperB));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB));

        when(signA.getRvvCode()).thenReturn(RVV_CODE_A);

        when(predicateA.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signA)).thenReturn(Boolean.TRUE);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        // Verify that it actually uses all the signMappers available
        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Map.of(RVV_CODE_A, List.of(signA))));
        verify(signMapperB).addToDto(resultCaptor.capture(), eq(Map.of(RVV_CODE_A, List.of(signA))));

        assertEquals(result, resultCaptor.getValue());
    }

    @Test
    void test_ok_allPredicatesTrueAndMultipleTrafficSignsAreMappedAndGroupedBySameRvvCode() {

        trafficSigns = List.of(signA, signB);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB));

        // Same rvv code
        when(signA.getRvvCode()).thenReturn(RVV_CODE_A);
        when(signB.getRvvCode()).thenReturn(RVV_CODE_A);

        when(predicateA.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateA.test(signB)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signB)).thenReturn(Boolean.TRUE);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        // results grouped by same rvv code
        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Map.of(RVV_CODE_A, List.of(signA, signB))));

        assertEquals(result, resultCaptor.getValue());
    }

    @Test
    void test_ok_allPredicatesTrueAndMultipleTrafficSignsAreMappedAndGroupedByDifferentRvvCode() {

        trafficSigns = List.of(signA, signB);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB));

        // different rvv codes
        when(signA.getRvvCode()).thenReturn(RVV_CODE_A);
        when(signB.getRvvCode()).thenReturn(RVV_CODE_B);

        when(predicateA.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateA.test(signB)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signB)).thenReturn(Boolean.TRUE);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        // map grouped by different rvv codes
        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Map.of(RVV_CODE_A, List.of(signA),
                RVV_CODE_B, List.of(signB))));

        assertEquals(result, resultCaptor.getValue());
    }


    @Test
    void test_ok_predicatesAFalseAndTrafficSignExcluded() {

        trafficSigns = List.of(signA);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB));

        // predicate a filters out this traffic sign
        when(predicateA.test(signA)).thenReturn(Boolean.FALSE);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Collections.emptyMap()));

        // when predicate a is false, b is never tested when using AND
        verifyNoInteractions(signMapperB);

        assertEquals(result, resultCaptor.getValue());
    }

    @Test
    void test_ok_predicatesBFalseAndTrafficSignExcluded() {

        trafficSigns = List.of(signA);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB));

        when(predicateA.test(signA)).thenReturn(Boolean.TRUE);
        // predicate b is filtering out the traffic sign
        when(predicateB.test(signA)).thenReturn(Boolean.FALSE);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Collections.emptyMap()));

        assertEquals(result, resultCaptor.getValue());
    }
}
