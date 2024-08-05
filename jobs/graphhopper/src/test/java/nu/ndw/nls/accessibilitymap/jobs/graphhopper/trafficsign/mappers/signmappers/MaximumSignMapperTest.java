package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.SignMapper.DtoSetter;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.LocationJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.RoadJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaximumSignMapperTest {

    private static final String MAXIMUM_STRING = "5,4";
    private static final String MAXIMUM_STRING_MORE_RESTRICTIVE = "4,8";
    private static final double MAXIMUM_DOUBLE = 5.4;
    private static final double MAXIMUM_DOUBLE_MORE_RESTRICTIVE = 4.8;

    private static final String RVV_CODE = "C17";
    private static final String OTHER_RVV_CODE = "C18";
    private static final String ROAD_SECTION_ID = "ID";
    private static final DirectionalDto<Double> NO_RESTRICTIONS = new DirectionalDto<>(Double.POSITIVE_INFINITY);
    @Mock
    private DtoSetter<Double> setter;
    @Mock
    private TrafficSignAccessibilityDto dto;
    @Mock
    private TrafficSignJsonDtoV3 signA;
    @Mock
    private TrafficSignJsonDtoV3 signB;
    @Mock
    private LocationJsonDtoV3 locationA;
    @Mock
    private LocationJsonDtoV3 locationB;
    @Mock
    private RoadJsonDtoV3 road;
    @Captor
    private ArgumentCaptor<DirectionalDto<Double>> setValueCaptor;

    private MaximumSignMapper maximumSignToDtoMapper;

    @BeforeEach
    void setup() {
        this.maximumSignToDtoMapper = new MaximumSignMapper(RVV_CODE, setter);
    }

    @Test
    void getRvvCodesUsed_ok_allValuesPresent() {
        assertEquals(RVV_CODE, maximumSignToDtoMapper.getRvvCode());
    }

    @Test
    void addToDto_ok_forward() {
        when(signA.getBlackCode()).thenReturn(MAXIMUM_STRING);
        when(signA.getLocation()).thenReturn(locationA);
        when(locationA.getDrivingDirection()).thenReturn("H");

        DirectionalDto<Double> expectedDirectional = DirectionalDto.<Double>builder()
                .forward(MAXIMUM_DOUBLE)
                .reverse(Double.POSITIVE_INFINITY)
                .build();

        testMapping(expectedDirectional, List.of(signA));
    }

    @Test
    void addToDto_ok_reverse() {
        when(signA.getBlackCode()).thenReturn(MAXIMUM_STRING);
        when(signA.getLocation()).thenReturn(locationA);
        when(locationA.getDrivingDirection()).thenReturn("T");

        DirectionalDto<Double> expectedDirectional = DirectionalDto.<Double>builder()
                .forward(Double.POSITIVE_INFINITY)
                .reverse(MAXIMUM_DOUBLE)
                .build();

        testMapping(expectedDirectional, List.of(this.signA));
    }

    @Test
    void addToDto_ok_bothWays() {
        when(signA.getBlackCode()).thenReturn(MAXIMUM_STRING);
        when(signA.getLocation()).thenReturn(locationA);
        when(locationA.getDrivingDirection()).thenReturn(null);

        DirectionalDto<Double> expectedDirectional = DirectionalDto.<Double>builder()
                .forward(MAXIMUM_DOUBLE)
                .reverse(MAXIMUM_DOUBLE)
                .build();

        testMapping(expectedDirectional, List.of(this.signA));
    }

    @Test
    void addToDto_ok_multipleSigns() {
        when(signA.getBlackCode()).thenReturn(MAXIMUM_STRING_MORE_RESTRICTIVE);
        when(signA.getLocation()).thenReturn(locationA);
        when(locationA.getDrivingDirection()).thenReturn("H");

        when(signB.getBlackCode()).thenReturn(MAXIMUM_STRING);
        when(signB.getLocation()).thenReturn(locationB);
        when(locationB.getDrivingDirection()).thenReturn(null);

        DirectionalDto<Double> expectedDirectional = DirectionalDto.<Double>builder()
                .forward(MAXIMUM_DOUBLE_MORE_RESTRICTIVE)
                .reverse(MAXIMUM_DOUBLE)
                .build();

        testMapping(expectedDirectional, List.of(signA, signB));
    }

    @Test
    void addToDto_ok_noSigns() {
        testMapping(NO_RESTRICTIONS, Map.of(OTHER_RVV_CODE, List.of(signA)));
    }

    @Test
    void setLinkTags_ok_blackCodeNull() {
        when(signA.getBlackCode()).thenReturn(null);
        when(signA.getLocation()).thenReturn(locationA);
        when(signA.getLocation().getDrivingDirection()).thenReturn(null);

        testMapping(NO_RESTRICTIONS, List.of(signA));
    }

    @Test
    void setLinkTags_ok_unsupportedBlackCode() {
        when(signA.getBlackCode()).thenReturn("10 m");
        when(signA.getLocation()).thenReturn(locationA);
        when(locationA.getRoad()).thenReturn(road);
        when(road.getRoadSectionId()).thenReturn(ROAD_SECTION_ID);

        testMapping(NO_RESTRICTIONS, List.of(signA));
    }

    private void testMapping(DirectionalDto<Double> expected, List<TrafficSignJsonDtoV3> trafficSigns) {
        Map<String, List<TrafficSignJsonDtoV3>> trafficSignMap = Map.of(RVV_CODE, trafficSigns);
        testMapping(expected, trafficSignMap);
    }

    private void testMapping(DirectionalDto<Double> expected, Map<String, List<TrafficSignJsonDtoV3>> trafficSignMap) {
        maximumSignToDtoMapper.addToDto(dto, trafficSignMap);

        verify(setter).set(eq(dto), setValueCaptor.capture());
        assertEquals(expected, setValueCaptor.getValue());
    }

}