package nu.ndw.nls.accessibilitymap.backend.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.backend.controllers.AccessibilityMapApiDelegateImpl.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.exceptions.VehicleWeightRequiredException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.RequestMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.ResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.services.AccessibilityMapService;
import nu.ndw.nls.routingmapmatcher.domain.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private static final float VEHICLE_AXLE_WEIGHT = 5F;
    private static final String MUNICIPALITY_ID = "GM0344";
    @Captor
    private ArgumentCaptor<VehicleArguments> vehicleArgumentsArgumentCaptor;
    @Mock
    private RequestMapper requestMapper;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private AccessibilityMapService accessibilityMapService;

    @InjectMocks
    private AccessibilityMapApiDelegateImpl accessibilityMapApiDelegate;

    @Test
    void getInaccessibleRoadSections_ok() {
        VehicleProperties vehicleProperties = VehicleProperties.builder().build();
        Set<IsochroneMatch> isochroneMatches = Set.of(IsochroneMatch.builder().build());
        RoadSectionsJson roadSectionsJson = new RoadSectionsJson();

        when(requestMapper.mapToVehicleProperties(vehicleArgumentsArgumentCaptor.capture()))
                .thenReturn(vehicleProperties);
        when(accessibilityMapService.calculateInaccessibleRoadSections(vehicleProperties, MUNICIPALITY_ID))
                .thenReturn(isochroneMatches);
        when(responseMapper.mapToRoadSectionsJson(isochroneMatches))
                .thenReturn(roadSectionsJson);

        ResponseEntity<RoadSectionsJson> response = accessibilityMapApiDelegate.getInaccessibleRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR,
                VEHICLE_LENGTH,
                VEHICLE_WIDTH,
                VEHICLE_HEIGHT,
                VEHICLE_WEIGHT,
                VEHICLE_AXLE_WEIGHT,
                false);

        VehicleArguments expectedVehicleArguments = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.CAR)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(VEHICLE_WEIGHT)
                .vehicleAxleWeight(VEHICLE_AXLE_WEIGHT)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHasTrailer(false)
                .build();
        VehicleArguments vehicleArguments = vehicleArgumentsArgumentCaptor.getValue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roadSectionsJson);
        assertThat(vehicleArguments).isEqualTo(expectedVehicleArguments);
    }


    @Test
    void getInaccessibleRoadSections_with_no_weight_exception() {

        VehicleWeightRequiredException exception = assertThrows(VehicleWeightRequiredException.class,
                () -> accessibilityMapApiDelegate.getInaccessibleRoadSections(
                        MUNICIPALITY_ID,
                        VehicleTypeJson.COMMERCIAL_VEHICLE,
                        VEHICLE_LENGTH,
                        VEHICLE_WIDTH,
                        VEHICLE_HEIGHT,
                        null,
                        VEHICLE_AXLE_WEIGHT,
                        false));

        assertThat(exception.getMessage()).isEqualTo("When selecting 'commercial_vehicle' as vehicle type "
                + "vehicle weight is required");
    }
}
