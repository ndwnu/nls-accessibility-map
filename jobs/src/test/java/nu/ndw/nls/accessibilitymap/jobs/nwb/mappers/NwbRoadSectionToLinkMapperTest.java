package nu.ndw.nls.accessibilitymap.jobs.nwb.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers.TrafficSignToDtoMapper;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbRoadSectionToLinkMapperTest {

    private static final double GEOMETRY_LENGTH = 10;
    private static final int ROAD_SECTION_ID = 3;
    private static final long JUNCTION_ID_FROM = 1;
    private static final long JUNCTION_ID_TO = 2;
    private static final String DRIVING_DIRECTION_FORWARD = "H";
    private static final String DRIVING_DIRECTION_BACKWARD = "T";
    private static final String DRIVING_DIRECTION_BOTH = "B";
    private static final int MUNICIPALITY_ID = 307;

    @Mock
    private TrafficSignToDtoMapper trafficSignToDtoMapper;

    @InjectMocks
    private NwbRoadSectionToLinkMapperImpl nwbRoadSectionToLinkMapper;

    @Mock
    private LineString lineString;

    @Mock
    private List<TrafficSignJsonDtoV3> trafficSignJsonDtoV3s;
    @Mock
    private TrafficSignAccessibilityDto trafficSignAccessibilityDto;

    @Mock
    private DirectionalDto<Boolean> carAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> carAccessForbiddenWindowed;
    @Mock
    private DirectionalDto<Boolean> hgvAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> hgvAccessForbiddenWindowed;
    @Mock
    private DirectionalDto<Boolean> busAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> hgvAndBusAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> hgvAndBusAccessForbiddenWindowed;
    @Mock
    private DirectionalDto<Boolean> tractorAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> slowVehicleAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> trailerAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> motorcycleAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> motorVehicleAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> motorVehicleAccessForbiddenWindowed;
    @Mock
    private DirectionalDto<Boolean> lcvAndHgvAccessForbidden;
    @Mock
    private DirectionalDto<Boolean> lcvAndHgvAccessForbiddenWindowed;
    @Mock
    private DirectionalDto<Double> maxLength;
    @Mock
    private DirectionalDto<Double> maxWidth;
    @Mock
    private DirectionalDto<Double> maxHeight;
    @Mock
    private DirectionalDto<Double> maxAxleLoad;
    @Mock
    private DirectionalDto<Double> maxWeight;


    @BeforeEach
    void setUp() {
        when(lineString.getLength()).thenReturn(GEOMETRY_LENGTH);
        when(trafficSignToDtoMapper.map(trafficSignJsonDtoV3s)).thenReturn(trafficSignAccessibilityDto);

        when(trafficSignAccessibilityDto.getCarAccessForbidden()).thenReturn(carAccessForbidden);
        when(trafficSignAccessibilityDto.getCarAccessForbiddenWindowed()).thenReturn(carAccessForbiddenWindowed);
        when(trafficSignAccessibilityDto.getHgvAccessForbidden()).thenReturn(hgvAccessForbidden);
        when(trafficSignAccessibilityDto.getHgvAccessForbiddenWindowed()).thenReturn(hgvAccessForbiddenWindowed);
        when(trafficSignAccessibilityDto.getBusAccessForbidden()).thenReturn(busAccessForbidden);
        when(trafficSignAccessibilityDto.getHgvAndBusAccessForbidden()).thenReturn(hgvAndBusAccessForbidden);
        when(trafficSignAccessibilityDto.getHgvAndBusAccessForbiddenWindowed()).thenReturn(hgvAndBusAccessForbiddenWindowed);
        when(trafficSignAccessibilityDto.getTractorAccessForbidden()).thenReturn(tractorAccessForbidden);
        when(trafficSignAccessibilityDto.getSlowVehicleAccessForbidden()).thenReturn(slowVehicleAccessForbidden);
        when(trafficSignAccessibilityDto.getTrailerAccessForbidden()).thenReturn(trailerAccessForbidden);
        when(trafficSignAccessibilityDto.getMotorcycleAccessForbidden()).thenReturn(motorcycleAccessForbidden);
        when(trafficSignAccessibilityDto.getMotorVehicleAccessForbidden()).thenReturn(motorVehicleAccessForbidden);
        when(trafficSignAccessibilityDto.getMotorVehicleAccessForbiddenWindowed()).thenReturn(motorVehicleAccessForbiddenWindowed);
        when(trafficSignAccessibilityDto.getLcvAndHgvAccessForbidden()).thenReturn(lcvAndHgvAccessForbidden);
        when(trafficSignAccessibilityDto.getLcvAndHgvAccessForbiddenWindowed()).thenReturn(lcvAndHgvAccessForbiddenWindowed);
        when(trafficSignAccessibilityDto.getMaxLength()).thenReturn(maxLength);
        when(trafficSignAccessibilityDto.getMaxWidth()).thenReturn(maxWidth);
        when(trafficSignAccessibilityDto.getMaxHeight()).thenReturn(maxHeight);
        when(trafficSignAccessibilityDto.getMaxAxleLoad()).thenReturn(maxAxleLoad);
        when(trafficSignAccessibilityDto.getMaxWeight()).thenReturn(maxWeight);
    }


    @Test
    void map_ok() {
        AccessibilityLink link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_FORWARD),
                trafficSignJsonDtoV3s);

        assertEquals(ROAD_SECTION_ID, link.getId());
        assertEquals(JUNCTION_ID_FROM, link.getFromNodeId());
        assertEquals(JUNCTION_ID_TO, link.getToNodeId());
        assertTrue(link.getAccessibility().forward());
        assertFalse(link.getAccessibility().reverse());
        assertEquals(GEOMETRY_LENGTH, link.getDistanceInMeters());
        assertEquals(lineString, link.getGeometry());
        assertEquals(307, link.getMunicipalityCode());

        assertEquals(carAccessForbidden, link.getCarAccessForbidden());
        assertEquals(carAccessForbiddenWindowed, link.getCarAccessForbiddenWindowed());
        assertEquals(hgvAccessForbidden, link.getHgvAccessForbidden());
        assertEquals(hgvAccessForbiddenWindowed, link.getHgvAccessForbiddenWindowed());
        assertEquals(busAccessForbidden, link.getBusAccessForbidden());
        assertEquals(hgvAndBusAccessForbidden, link.getHgvAndBusAccessForbidden());
        assertEquals(hgvAndBusAccessForbiddenWindowed, link.getHgvAndBusAccessForbiddenWindowed());
        assertEquals(tractorAccessForbidden, link.getTractorAccessForbidden());
        assertEquals(slowVehicleAccessForbidden, link.getSlowVehicleAccessForbidden());
        assertEquals(trailerAccessForbidden, link.getTrailerAccessForbidden());
        assertEquals(motorcycleAccessForbidden, link.getMotorcycleAccessForbidden());
        assertEquals(motorVehicleAccessForbidden, link.getMotorVehicleAccessForbidden());
        assertEquals(motorVehicleAccessForbiddenWindowed, link.getMotorVehicleAccessForbiddenWindowed());
        assertEquals(lcvAndHgvAccessForbidden, link.getLcvAndHgvAccessForbidden());
        assertEquals(lcvAndHgvAccessForbiddenWindowed, link.getLcvAndHgvAccessForbiddenWindowed());
        assertEquals(maxLength, link.getMaxLength());
        assertEquals(maxWidth, link.getMaxWidth());
        assertEquals(maxHeight, link.getMaxHeight());
        assertEquals(maxAxleLoad, link.getMaxAxleLoad());
        assertEquals(maxWeight, link.getMaxWeight());
    }

    @Test
    void map_ok_drivingDirectionBackward() {
        AccessibilityLink link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_BACKWARD),
                trafficSignJsonDtoV3s);

        assertFalse(link.getAccessibility().forward());
        assertTrue(link.getAccessibility().reverse());
    }

    @Test
    void map_ok_drivingDirectionBoth() {
        AccessibilityLink link = nwbRoadSectionToLinkMapper.map(createRoadSectionDto(DRIVING_DIRECTION_BOTH),
                trafficSignJsonDtoV3s);

        assertTrue(link.getAccessibility().forward());
        assertTrue(link.getAccessibility().reverse());
    }

    private NwbRoadSectionDto createRoadSectionDto(String drivingDirection) {
        return NwbRoadSectionDto.builder()
                .roadSectionId(ROAD_SECTION_ID)
                .junctionIdFrom(JUNCTION_ID_FROM)
                .junctionIdTo(JUNCTION_ID_TO)
                .drivingDirection(drivingDirection)
                .municipalityId(MUNICIPALITY_ID)
                .geometry(lineString)
                .build();
    }
}
