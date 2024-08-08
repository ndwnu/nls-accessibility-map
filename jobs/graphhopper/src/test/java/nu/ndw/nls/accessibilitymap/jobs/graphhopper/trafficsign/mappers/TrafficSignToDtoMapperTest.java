package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.TrafficSignToDtoMapper.TrafficSignIncludedFilterPredicate;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.SignMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
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
    private TrafficSignPropertiesDto singAProperties;
    @Mock
    private TrafficSignGeoJsonDto signA;
    @Mock
    private TrafficSignPropertiesDto signBProperties;
    @Mock
    private TrafficSignGeoJsonDto signB;

    @Mock
    TrafficSignGeoJsonDto c6;
    @Mock
    TrafficSignGeoJsonDto c7;
    @Mock
    TrafficSignGeoJsonDto c7b;
    @Mock
    TrafficSignGeoJsonDto c12;

    @Mock
    TrafficSignGeoJsonDto c22c;
    @Mock
    private TrafficSignPropertiesDto c6TProperties;
    @Mock
    TrafficSignGeoJsonDto c6T;
    @Mock
    private TrafficSignPropertiesDto c7TProperties;
    @Mock
    TrafficSignGeoJsonDto c7T;
    @Mock
    private TrafficSignPropertiesDto c7bTProperties;
    @Mock
    TrafficSignGeoJsonDto c7bT;
    @Mock
    private TrafficSignPropertiesDto c12TProperties;
    @Mock
    TrafficSignGeoJsonDto c12T;
    @Mock
    private TrafficSignPropertiesDto c22cTProperties;
    @Mock
    TrafficSignGeoJsonDto c22cT;


    @Mock
    private SignMapper<?> signMapperA;

    @Mock
    private SignMapper<?> signMapperB;

    @Mock
    NoEntrySignWindowedMapper noEntrySignWindowedMapper;


    @Captor
    private ArgumentCaptor<TrafficSignAccessibilityDto> resultCaptor;

    private List<TrafficSignGeoJsonDto> trafficSigns;

    @Test
    void test_ok_allPredicatesTrueAndTrafficSignIsMappedBySingleMapper() {
        when(signA.getProperties()).thenReturn(singAProperties);
        trafficSigns = List.of(signA);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB), noEntrySignWindowedMapper);

        when(signA.getProperties().getRvvCode()).thenReturn(RVV_CODE_A);

        when(predicateA.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signA)).thenReturn(Boolean.TRUE);

        when(noEntrySignWindowedMapper.map(signA)).thenReturn(signA);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Map.of(RVV_CODE_A, List.of(signA))));

        assertEquals(result, resultCaptor.getValue());
    }

    @Test
    void test_ok_allPredicatesTrueAndTrafficSignIsMappedByMultipleMappers() {
        when(signA.getProperties()).thenReturn(singAProperties);
        trafficSigns = List.of(signA);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA, signMapperB));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB), noEntrySignWindowedMapper);

        when(signA.getProperties().getRvvCode()).thenReturn(RVV_CODE_A);

        when(predicateA.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signA)).thenReturn(Boolean.TRUE);

        when(noEntrySignWindowedMapper.map(signA)).thenReturn(signA);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        // Verify that it actually uses all the signMappers available
        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Map.of(RVV_CODE_A, List.of(signA))));
        verify(signMapperB).addToDto(resultCaptor.capture(), eq(Map.of(RVV_CODE_A, List.of(signA))));

        assertEquals(result, resultCaptor.getValue());
    }

    @Test
    void test_ok_allPredicatesTrueAndMultipleTrafficSignsAreMappedAndGroupedBySameRvvCode() {
        when(signA.getProperties()).thenReturn(singAProperties);
        when(signB.getProperties()).thenReturn(signBProperties);
        trafficSigns = List.of(signA, signB);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB), noEntrySignWindowedMapper);

        // Same rvv code
        when(signA.getProperties().getRvvCode()).thenReturn(RVV_CODE_A);
        when(signB.getProperties().getRvvCode()).thenReturn(RVV_CODE_A);

        when(noEntrySignWindowedMapper.map(signA)).thenReturn(signA);
        when(noEntrySignWindowedMapper.map(signB)).thenReturn(signB);

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
        when(signA.getProperties()).thenReturn(singAProperties);
        when(signB.getProperties()).thenReturn(signBProperties);
        trafficSigns = List.of(signA, signB);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB), noEntrySignWindowedMapper);

        // different rvv codes
        when(signA.getProperties().getRvvCode()).thenReturn(RVV_CODE_A);
        when(signB.getProperties().getRvvCode()).thenReturn(RVV_CODE_B);

        when(predicateA.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signA)).thenReturn(Boolean.TRUE);
        when(predicateA.test(signB)).thenReturn(Boolean.TRUE);
        when(predicateB.test(signB)).thenReturn(Boolean.TRUE);

        when(noEntrySignWindowedMapper.map(signA)).thenReturn(signA);
        when(noEntrySignWindowedMapper.map(signB)).thenReturn(signB);

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
                List.of(predicateA, predicateB), noEntrySignWindowedMapper);

        // predicate a filters out this traffic sign
        when(predicateA.test(signA)).thenReturn(Boolean.FALSE);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Collections.emptyMap()));

        // when predicate a is false, b is never tested when using AND
        verifyNoInteractions(signMapperB);

        assertEquals(result, resultCaptor.getValue());
    }

    @Test
    void test_ok_windowedTrafficSigns() {
        trafficSigns = List.of(c6, c7, c7b, c12, c22c);

        when(c6TProperties.getRvvCode()).thenReturn("C6T");
        when(c7TProperties.getRvvCode()).thenReturn("C7T");
        when(c7bTProperties.getRvvCode()).thenReturn("C7bT");
        when(c12TProperties.getRvvCode()).thenReturn("C12T");
        when(c22cTProperties.getRvvCode()).thenReturn("C22cT");

        when(c6T.getProperties()).thenReturn(c6TProperties);
        when(c7T.getProperties()).thenReturn(c7TProperties);
        when(c7bT.getProperties()).thenReturn(c7bTProperties);
        when(c12T.getProperties()).thenReturn(c12TProperties);
        when(c22cT.getProperties()).thenReturn(c22cTProperties);

        when(trafficSignMapperRegistry.getMappers()).thenReturn(List.of(signMapperA));

        trafficSignToDtoMapper = new TrafficSignToDtoMapper(trafficSignMapperRegistry,
                List.of(predicateA, predicateB), noEntrySignWindowedMapper);

        when(predicateA.test(c6)).thenReturn(Boolean.TRUE);
        when(predicateB.test(c6)).thenReturn(Boolean.TRUE);

        when(predicateA.test(c7)).thenReturn(Boolean.TRUE);
        when(predicateB.test(c7)).thenReturn(Boolean.TRUE);

        when(predicateA.test(c7b)).thenReturn(Boolean.TRUE);
        when(predicateB.test(c7b)).thenReturn(Boolean.TRUE);

        when(predicateA.test(c12)).thenReturn(Boolean.TRUE);
        when(predicateB.test(c12)).thenReturn(Boolean.TRUE);

        when(predicateA.test(c22c)).thenReturn(Boolean.TRUE);
        when(predicateB.test(c22c)).thenReturn(Boolean.TRUE);

        when(noEntrySignWindowedMapper.map(c6)).thenReturn(c6T);
        when(noEntrySignWindowedMapper.map(c7)).thenReturn(c7T);
        when(noEntrySignWindowedMapper.map(c7b)).thenReturn(c7bT);
        when(noEntrySignWindowedMapper.map(c12)).thenReturn(c12T);
        when(noEntrySignWindowedMapper.map(c22c)).thenReturn(c22cT);

        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(trafficSigns);

        verify(signMapperA).addToDto(resultCaptor.capture(), eq(Map.of(
                "C6T", List.of(c6T),
                "C7T", List.of(c7T),
                "C7bT", List.of(c7bT),
                "C12T", List.of(c12T),
                "C22cT", List.of(c22cT)
        )));

        assertEquals(result, resultCaptor.getValue());
    }


}

