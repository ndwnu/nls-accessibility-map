package nu.ndw.nls.accessibilitymap.backend.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.SortedMap;
import nu.ndw.nls.accessibilitymap.backend.controllers.AccessibilityMapApiDelegateImpl.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.exceptions.VehicleWeightRequiredException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.PointMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.RequestMapper;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.backend.services.PointMatchService;
import nu.ndw.nls.accessibilitymap.backend.validators.PointValidator;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.domain.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapApiDelegateImplTest {

    private static final float VEHICLE_LENGTH = 1F;
    private static final float VEHICLE_WIDTH = 2F;
    private static final float VEHICLE_HEIGHT = 3F;
    private static final float VEHICLE_WEIGHT = 4F;
    private static final float VEHICLE_AXLE_LOAD = 5F;
    private static final String MUNICIPALITY_ID = "GM0344";
    public static final int REQUESTED_ROAD_SECTION_ID = 123;
    public static final double REQUESTED_LONGITUDE = 3333;
    public static final double REQUESTED_LATITUDE = 222;

    @Captor
    private ArgumentCaptor<VehicleArguments> vehicleArgumentsArgumentCaptor;
    @Mock
    private RequestMapper requestMapper;
    @Mock
    private AccessibilityResponseMapper accessibilityResponseMapper;
    @Mock
    private AccessibilityMapService accessibilityMapService;
    @Mock
    private SortedMap<Integer, RoadSection> idToRoadSectionMap;
    @Mock
    private PointValidator pointValidator;
    @Mock
    private PointMapper pointMapper;
    @Mock
    private PointMatchService pointMatchService;
    @Mock
    private Point requestedPoint;
    @Mock
    private CandidateMatch candidateMatch;

    @InjectMocks
    private AccessibilityMapApiDelegateImpl accessibilityMapApiDelegate;

    @Test
    void getInaccessibleRoadSections_ok() {
        VehicleProperties vehicleProperties = VehicleProperties.builder().build();
        AccessibilityMapResponseJson accessibilityMapResponseJson = new AccessibilityMapResponseJson();

        when(requestMapper.mapToVehicleProperties(vehicleArgumentsArgumentCaptor.capture()))
                .thenReturn(vehicleProperties);
        when(pointMapper.mapCoordinateAllowNulls(REQUESTED_LATITUDE, REQUESTED_LONGITUDE)).thenReturn(requestedPoint);
        when(pointMatchService.match(requestedPoint)).thenReturn(Optional.of(candidateMatch));
        when(candidateMatch.getMatchedLinkId()).thenReturn(REQUESTED_ROAD_SECTION_ID);
        when(accessibilityMapService.determineAccessibilityByRoadSection(vehicleProperties, MUNICIPALITY_ID))
                .thenReturn(idToRoadSectionMap);
        when(accessibilityResponseMapper.map(idToRoadSectionMap, REQUESTED_ROAD_SECTION_ID))
                .thenReturn(accessibilityMapResponseJson);

        ResponseEntity<AccessibilityMapResponseJson> response = accessibilityMapApiDelegate.getInaccessibleRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR,
                VEHICLE_LENGTH,
                VEHICLE_WIDTH,
                VEHICLE_HEIGHT,
                VEHICLE_WEIGHT,
                VEHICLE_AXLE_LOAD,
                false, REQUESTED_LATITUDE, REQUESTED_LONGITUDE);

        VehicleArguments expectedVehicleArguments = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.CAR)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(VEHICLE_WEIGHT)
                .vehicleAxleLoad(VEHICLE_AXLE_LOAD)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHasTrailer(false)
                .build();
        VehicleArguments vehicleArguments = vehicleArgumentsArgumentCaptor.getValue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(accessibilityMapResponseJson);
        assertThat(vehicleArguments).isEqualTo(expectedVehicleArguments);

        verify(pointValidator).validateConsistentValues(REQUESTED_LATITUDE, REQUESTED_LONGITUDE);
    }

    @Test
    void getInaccessibleRoadSections_exception_noWeight() {
        VehicleWeightRequiredException exception = assertThrows(VehicleWeightRequiredException.class,
                () -> accessibilityMapApiDelegate.getInaccessibleRoadSections(
                        MUNICIPALITY_ID,
                        VehicleTypeJson.COMMERCIAL_VEHICLE,
                        VEHICLE_LENGTH,
                        VEHICLE_WIDTH,
                        VEHICLE_HEIGHT,
                        null,
                        VEHICLE_AXLE_LOAD,
                        false, REQUESTED_LATITUDE, REQUESTED_LONGITUDE));

        assertThat(exception.getMessage()).isEqualTo("When selecting 'commercial_vehicle' as vehicle type "
                + "vehicle weight is required");
    }
}
