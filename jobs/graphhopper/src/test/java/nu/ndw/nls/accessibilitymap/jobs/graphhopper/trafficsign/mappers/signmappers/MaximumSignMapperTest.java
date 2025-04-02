package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.SignMapper.DtoSetter;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
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
    private static final Long ROAD_SECTION_ID = 1L;
    private static final DirectionalDto<Double> NO_RESTRICTIONS = new DirectionalDto<>(Double.POSITIVE_INFINITY);
    @Mock
    private DtoSetter<Double> setter;
    @Mock
    private TrafficSignAccessibilityDto dto;
    @Mock
    private TrafficSignGeoJsonDto signA;
    @Mock
    private TrafficSignGeoJsonDto signB;
    @Mock
    private TrafficSignPropertiesDto propertiesA;
    @Mock
    private TrafficSignPropertiesDto propertiesB;

    @Captor
    private ArgumentCaptor<DirectionalDto<Double>> setValueCaptor;

    private MaximumSignMapper maximumSignToDtoMapper;

    @BeforeEach
    void setup() {
        this.maximumSignToDtoMapper = new MaximumSignMapper(RVV_CODE, setter);
    }

    @Test
    void getRvvCodesUsed_allValuesPresent() {
        assertEquals(RVV_CODE, maximumSignToDtoMapper.getRvvCode());
    }

    @Test
    void addToDto_forward() {
        when(signA.getProperties()).thenReturn(propertiesA);
        when(propertiesA.getBlackCode()).thenReturn(MAXIMUM_STRING);
        when(propertiesA.getDrivingDirection()).thenReturn(DirectionType.FORTH);

        DirectionalDto<Double> expectedDirectional = DirectionalDto.<Double>builder()
                .forward(MAXIMUM_DOUBLE)
                .reverse(Double.POSITIVE_INFINITY)
                .build();

        testMapping(expectedDirectional, List.of(signA));
    }

    @Test
    void addToDto_reverse() {
        when(signA.getProperties()).thenReturn(propertiesA);
        when(propertiesA.getBlackCode()).thenReturn(MAXIMUM_STRING);
        when(propertiesA.getDrivingDirection()).thenReturn(DirectionType.BACK);

        DirectionalDto<Double> expectedDirectional = DirectionalDto.<Double>builder()
                .forward(Double.POSITIVE_INFINITY)
                .reverse(MAXIMUM_DOUBLE)
                .build();

        testMapping(expectedDirectional, List.of(this.signA));
    }

    @Test
    void addToDto_bothWays() {
        when(signA.getProperties()).thenReturn(propertiesA);
        when(propertiesA.getBlackCode()).thenReturn(MAXIMUM_STRING);
        when(propertiesA.getDrivingDirection()).thenReturn(null);

        DirectionalDto<Double> expectedDirectional = DirectionalDto.<Double>builder()
                .forward(MAXIMUM_DOUBLE)
                .reverse(MAXIMUM_DOUBLE)
                .build();

        testMapping(expectedDirectional, List.of(this.signA));
    }

    @Test
    void addToDto_multipleSigns() {
        when(signA.getProperties()).thenReturn(propertiesA);
        when(propertiesA.getBlackCode()).thenReturn(MAXIMUM_STRING_MORE_RESTRICTIVE);
        when(propertiesA.getDrivingDirection()).thenReturn(DirectionType.FORTH);

        when(signB.getProperties()).thenReturn(propertiesB);
        when(propertiesB.getBlackCode()).thenReturn(MAXIMUM_STRING);
        when(propertiesB.getDrivingDirection()).thenReturn(DirectionType.BACK);

        DirectionalDto<Double> expectedDirectional = DirectionalDto.<Double>builder()
                .forward(MAXIMUM_DOUBLE_MORE_RESTRICTIVE)
                .reverse(MAXIMUM_DOUBLE)
                .build();

        testMapping(expectedDirectional, List.of(signA, signB));
    }

    @Test
    void addToDto_noSigns() {
        testMapping(NO_RESTRICTIONS, Map.of(OTHER_RVV_CODE, List.of(signA)));
    }

    @Test
    void setLinkTags_blackCodeNull() {
        when(signA.getProperties()).thenReturn(propertiesA);
        when(propertiesA.getBlackCode()).thenReturn(null);
        when(propertiesA.getDrivingDirection()).thenReturn(null);

        testMapping(NO_RESTRICTIONS, List.of(signA));
    }

    @Test
    void setLinkTags_unsupportedBlackCode() {
        when(signA.getProperties()).thenReturn(propertiesA);
        when(propertiesA.getBlackCode()).thenReturn("10 m");
        when(propertiesA.getRoadSectionId()).thenReturn(ROAD_SECTION_ID);

        testMapping(NO_RESTRICTIONS, List.of(signA));
    }

    private void testMapping(DirectionalDto<Double> expected, List<TrafficSignGeoJsonDto> trafficSigns) {
        Map<String, List<TrafficSignGeoJsonDto>> trafficSignMap = Map.of(RVV_CODE, trafficSigns);
        testMapping(expected, trafficSignMap);
    }

    private void testMapping(DirectionalDto<Double> expected,
            Map<String, List<TrafficSignGeoJsonDto>> trafficSignMap) {
        maximumSignToDtoMapper.addToDto(dto, trafficSignMap);

        verify(setter).set(eq(dto), setValueCaptor.capture());
        assertEquals(expected, setValueCaptor.getValue());
    }

}