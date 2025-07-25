package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.util.shapes.BBox;
import java.time.OffsetDateTime;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestMapperTest {

    private static final float DEFAULT_VEHICLE_AXLE_LOAD = 4.0F;

    private static final float DEFAULT_VEHICLE_HEIGHT = 1.0F;

    private static final float DEFAULT_VEHICLE_LENGTH = 10.0F;

    private static final float DEFAULT_VEHICLE_WIDTH = 2.0F;

    private static final double DEFAULT_SEARCH_DISTANCE = 50000;

    private static final int DEFAULT_MUNICIPALITY_ID = 307;

    private static final double DEFAULT_LONGITUDE_COORDINATE = 0.0;

    private static final double DEFAULT_LATITUDE_COORDINATE = 1.0;

    private static final double MAX_LONGITUDE = 2.0;

    private static final double MAX_LATITUDE = 3.0;

    private static final float DEFAULT_VEHICLE_WEIGHT = 1000.0F;

    private AccessibilityRequestMapper accessibilityRequestMapper;

    @Mock
    private TransportTypeMapper transportTypeV2Mapper;

    @Mock
    private FuelTypeMapper fuelTypeMapper;

    @Mock
    private EmissionClassMapper emissionClassMapper;

    @Mock
    private VehicleArguments vehicleArguments;

    @Mock
    private Municipality municipality;

    @Mock
    private MunicipalityBoundingBox municipalityBoundingBox;

    @Mock
    private OffsetDateTime timestamp;

    @BeforeEach
    void setUp() {
        accessibilityRequestMapper = new AccessibilityRequestMapper(transportTypeV2Mapper, emissionClassMapper, fuelTypeMapper);
    }

    @Test
    void mapToAccessibilityRequest_mapsFieldsCorrectly() {

        when(vehicleArguments.vehicleAxleLoad()).thenReturn(DEFAULT_VEHICLE_AXLE_LOAD);
        when(vehicleArguments.vehicleHeight()).thenReturn(DEFAULT_VEHICLE_HEIGHT);
        when(vehicleArguments.vehicleLength()).thenReturn(DEFAULT_VEHICLE_LENGTH);
        when(vehicleArguments.vehicleWidth()).thenReturn(DEFAULT_VEHICLE_WIDTH);
        when(vehicleArguments.vehicleWeight()).thenReturn(DEFAULT_VEHICLE_WEIGHT);
        when(vehicleArguments.emissionClass()).thenReturn(EmissionClassJson.EURO_1);
        when(vehicleArguments.fuelType()).thenReturn(FuelTypeJson.PETROL);

        when(municipality.municipalityIdAsInteger()).thenReturn(DEFAULT_MUNICIPALITY_ID);
        when(municipality.searchDistanceInMetres()).thenReturn((int) DEFAULT_SEARCH_DISTANCE);
        when(municipality.startCoordinateLatitude()).thenReturn(DEFAULT_LATITUDE_COORDINATE);
        when(municipality.startCoordinateLongitude()).thenReturn(DEFAULT_LONGITUDE_COORDINATE);
        when(municipality.bounds()).thenReturn(municipalityBoundingBox);

        when(municipalityBoundingBox.longitudeFrom()).thenReturn(DEFAULT_LONGITUDE_COORDINATE);
        when(municipalityBoundingBox.latitudeFrom()).thenReturn(DEFAULT_LATITUDE_COORDINATE);
        when(municipalityBoundingBox.longitudeTo()).thenReturn(MAX_LONGITUDE);
        when(municipalityBoundingBox.latitudeTo()).thenReturn(MAX_LATITUDE);

        when(transportTypeV2Mapper.mapToTransportType(vehicleArguments)).thenReturn(Set.of(TransportType.CAR));
        when(emissionClassMapper.mapEmissionClass(EmissionClassJson.EURO_1)).thenReturn(Set.of(EmissionClass.EURO_1));
        when(fuelTypeMapper.mapFuelType(FuelTypeJson.PETROL)).thenReturn(Set.of(FuelType.PETROL));

        var accessibilityRequest = accessibilityRequestMapper.mapToAccessibilityRequest(timestamp, municipality, vehicleArguments);

        assertThat(accessibilityRequest).isEqualTo(AccessibilityRequest.builder()
                .timestamp(timestamp)
                .addMissingRoadsSectionsFromNwb(true)
                .boundingBox(BBox.fromPoints(DEFAULT_LATITUDE_COORDINATE, DEFAULT_LONGITUDE_COORDINATE, MAX_LATITUDE, MAX_LONGITUDE))
                .transportTypes(Set.of(TransportType.CAR))
                .vehicleHeightInCm((double) DEFAULT_VEHICLE_HEIGHT * 100)
                .vehicleLengthInCm((double) DEFAULT_VEHICLE_LENGTH * 100)
                .vehicleWidthInCm((double) DEFAULT_VEHICLE_WIDTH * 100)
                .vehicleWeightInKg((double) DEFAULT_VEHICLE_WEIGHT * 1_000)
                .vehicleAxleLoadInKg((double) DEFAULT_VEHICLE_AXLE_LOAD * 1_000)
                .startLocationLatitude(DEFAULT_LATITUDE_COORDINATE)
                .startLocationLongitude(DEFAULT_LONGITUDE_COORDINATE)
                .municipalityId(DEFAULT_MUNICIPALITY_ID)
                .emissionClasses(Set.of(EmissionClass.EURO_1))
                .fuelTypes(Set.of(FuelType.PETROL))
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
        when(vehicleArguments.emissionClass()).thenReturn(null);
        when(vehicleArguments.fuelType()).thenReturn(null);

        when(municipality.municipalityIdAsInteger()).thenReturn(DEFAULT_MUNICIPALITY_ID);
        when(municipality.searchDistanceInMetres()).thenReturn((int) DEFAULT_SEARCH_DISTANCE);
        when(municipality.startCoordinateLatitude()).thenReturn(DEFAULT_LATITUDE_COORDINATE);
        when(municipality.startCoordinateLongitude()).thenReturn(DEFAULT_LONGITUDE_COORDINATE);
        when(municipality.bounds()).thenReturn(municipalityBoundingBox);

        when(municipalityBoundingBox.longitudeFrom()).thenReturn(DEFAULT_LONGITUDE_COORDINATE);
        when(municipalityBoundingBox.latitudeFrom()).thenReturn(DEFAULT_LATITUDE_COORDINATE);
        when(municipalityBoundingBox.longitudeTo()).thenReturn(MAX_LONGITUDE);
        when(municipalityBoundingBox.latitudeTo()).thenReturn(MAX_LATITUDE);

        when(transportTypeV2Mapper.mapToTransportType(vehicleArguments)).thenReturn(Set.of(TransportType.CAR));
        when(fuelTypeMapper.mapFuelType(null)).thenReturn(null);
        when(emissionClassMapper.mapEmissionClass(null)).thenReturn(null);

        var accessibilityRequest = accessibilityRequestMapper.mapToAccessibilityRequest(timestamp, municipality, vehicleArguments);

        assertThat(accessibilityRequest).isEqualTo(AccessibilityRequest.builder()
                .timestamp(timestamp)
                .boundingBox(BBox.fromPoints(DEFAULT_LATITUDE_COORDINATE, DEFAULT_LONGITUDE_COORDINATE, MAX_LATITUDE, MAX_LONGITUDE))
                .transportTypes(Set.of(TransportType.CAR))
                .startLocationLongitude(DEFAULT_LONGITUDE_COORDINATE)
                .startLocationLatitude(DEFAULT_LATITUDE_COORDINATE)
                .municipalityId(DEFAULT_MUNICIPALITY_ID)
                .addMissingRoadsSectionsFromNwb(true)
                .searchRadiusInMeters(DEFAULT_SEARCH_DISTANCE)
                .build());
    }
}
