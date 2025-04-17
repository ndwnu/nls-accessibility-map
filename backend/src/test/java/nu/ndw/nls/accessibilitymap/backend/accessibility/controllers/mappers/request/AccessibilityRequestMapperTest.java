package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.util.shapes.BBox;
import java.time.OffsetDateTime;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.dto.MunicipalityBoundingBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestMapperTest {

    private static final float DEFAULT_VEHICLE_AXLE_LOAD = 4.0F;

    private static final float DEFAULT_VEHICLE_HEIGHT = 1.0F;

    private static final float DEFAULT_VEHICLE_LENGTH = 10.0F;

    private static final float DEFAULT_VEHICLE_WIDTH = 2.0F;

    private static final double DEFAULT_SEARCH_DISTANCE = 50000D;

    private static final int DEFAULT_MUNICIPALITY_ID = 307;

    private static final double DEFAULT_X_COORDINATE = 0.0;

    private static final double DEFAULT_Y_COORDINATE = 1.0;

    private static final double MAX_LONGITUDE = 2.0;

    private static final double MAX_LATITUDE = 3.0;

    private static final float DEFAULT_VEHICLE_WEIGHT = 1000.0F;

    private AccessibilityRequestMapper accessibilityRequestV2Mapper;

    @Mock
    private TransportTypeMapper transportTypeV2Mapper;

    @Mock
    private VehicleArguments vehicleArguments;

    @Mock
    private Municipality municipality;

    @Mock
    private Point point;

    @Mock
    private MunicipalityBoundingBox municipalityBoundingBox;

    @Mock
    private OffsetDateTime timestamp;

    @BeforeEach
    void setUp() {
        accessibilityRequestV2Mapper = new AccessibilityRequestMapper(transportTypeV2Mapper);
    }

    @Test
    void mapToAccessibilityRequest_mapsFieldsCorrectly() {

        when(vehicleArguments.vehicleAxleLoad()).thenReturn(DEFAULT_VEHICLE_AXLE_LOAD);
        when(vehicleArguments.vehicleHeight()).thenReturn(DEFAULT_VEHICLE_HEIGHT);
        when(vehicleArguments.vehicleLength()).thenReturn(DEFAULT_VEHICLE_LENGTH);
        when(vehicleArguments.vehicleWidth()).thenReturn(DEFAULT_VEHICLE_WIDTH);
        when(vehicleArguments.vehicleWeight()).thenReturn(DEFAULT_VEHICLE_WEIGHT);

        when(municipality.getMunicipalityIdInteger()).thenReturn(DEFAULT_MUNICIPALITY_ID);
        when(municipality.getSearchDistanceInMetres()).thenReturn(DEFAULT_SEARCH_DISTANCE);
        when(municipality.getStartPoint()).thenReturn(point);
        when(municipality.getBounds()).thenReturn(municipalityBoundingBox);

        when(point.getX()).thenReturn(DEFAULT_X_COORDINATE);
        when(point.getY()).thenReturn(DEFAULT_Y_COORDINATE);

        when(municipalityBoundingBox.longitudeFrom()).thenReturn(DEFAULT_X_COORDINATE);
        when(municipalityBoundingBox.latitudeFrom()).thenReturn(DEFAULT_Y_COORDINATE);
        when(municipalityBoundingBox.longitudeTo()).thenReturn(MAX_LONGITUDE);
        when(municipalityBoundingBox.latitudeTo()).thenReturn(MAX_LATITUDE);

        when(transportTypeV2Mapper.mapToTransportType(vehicleArguments)).thenReturn(Set.of(TransportType.CAR));

        var accessibilityRequest = accessibilityRequestV2Mapper.mapToAccessibilityRequest(timestamp, municipality, vehicleArguments);

        assertThat(accessibilityRequest).isEqualTo(AccessibilityRequest.builder()
                .timestamp(timestamp)
                .boundingBox(BBox.fromPoints(DEFAULT_Y_COORDINATE, DEFAULT_X_COORDINATE, MAX_LATITUDE, MAX_LONGITUDE))
                .transportTypes(Set.of(TransportType.CAR))
                .vehicleHeightInCm((double) DEFAULT_VEHICLE_HEIGHT)
                .vehicleWeightInKg((double) DEFAULT_VEHICLE_WEIGHT)
                .vehicleLengthInCm((double) DEFAULT_VEHICLE_LENGTH)
                .vehicleWidthInCm((double) DEFAULT_VEHICLE_WIDTH)
                .vehicleAxleLoadInKg((double) DEFAULT_VEHICLE_AXLE_LOAD)
                .startLocationLongitude(DEFAULT_X_COORDINATE)
                .startLocationLatitude(DEFAULT_Y_COORDINATE)
                .municipalityId(DEFAULT_MUNICIPALITY_ID)
                .searchRadiusInMeters(DEFAULT_SEARCH_DISTANCE)
                .build());
    }

    @Test
    void mapToAccessibilityRequest_handlesNullValuesGracefully() {

        when(vehicleArguments.vehicleAxleLoad()).thenReturn(null);
        when(vehicleArguments.vehicleHeight()).thenReturn(null);
        when(vehicleArguments.vehicleLength()).thenReturn(null);
        when(vehicleArguments.vehicleWidth()).thenReturn(null);
        when(vehicleArguments.vehicleWeight()).thenReturn(null);

        when(municipality.getMunicipalityIdInteger()).thenReturn(DEFAULT_MUNICIPALITY_ID);
        when(municipality.getSearchDistanceInMetres()).thenReturn(DEFAULT_SEARCH_DISTANCE);
        when(municipality.getStartPoint()).thenReturn(point);
        when(municipality.getBounds()).thenReturn(municipalityBoundingBox);

        when(point.getX()).thenReturn(DEFAULT_X_COORDINATE);
        when(point.getY()).thenReturn(DEFAULT_Y_COORDINATE);

        when(municipalityBoundingBox.longitudeFrom()).thenReturn(DEFAULT_X_COORDINATE);
        when(municipalityBoundingBox.latitudeFrom()).thenReturn(DEFAULT_Y_COORDINATE);
        when(municipalityBoundingBox.longitudeTo()).thenReturn(MAX_LONGITUDE);
        when(municipalityBoundingBox.latitudeTo()).thenReturn(MAX_LATITUDE);

        when(transportTypeV2Mapper.mapToTransportType(vehicleArguments)).thenReturn(Set.of(TransportType.CAR));

        var accessibilityRequest = accessibilityRequestV2Mapper.mapToAccessibilityRequest(timestamp, municipality, vehicleArguments);

        assertThat(accessibilityRequest).isEqualTo(AccessibilityRequest.builder()
                .timestamp(timestamp)
                .boundingBox(BBox.fromPoints(DEFAULT_Y_COORDINATE, DEFAULT_X_COORDINATE, MAX_LATITUDE, MAX_LONGITUDE))
                .transportTypes(Set.of(TransportType.CAR))
                .startLocationLongitude(DEFAULT_X_COORDINATE)
                .startLocationLatitude(DEFAULT_Y_COORDINATE)
                .municipalityId(DEFAULT_MUNICIPALITY_ID)
                .searchRadiusInMeters(DEFAULT_SEARCH_DISTANCE)
                .build());
    }
}
