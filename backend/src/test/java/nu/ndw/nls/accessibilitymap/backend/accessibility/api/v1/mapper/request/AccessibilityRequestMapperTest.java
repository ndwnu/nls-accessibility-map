package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.util.shapes.BBox;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.dto.Excludes;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.FuelTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.TransportTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.FuelTypeJson;
import nu.ndw.nls.springboot.core.time.ClockService;
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
    private EmissionZoneTypeMapper emissionZoneTypeMapper;

    @Mock
    private EmissionClassMapper emissionClassMapper;

    @Mock
    private Municipality municipality;

    @Mock
    private MunicipalityBoundingBox municipalityBoundingBox;

    @Mock
    private ClockService clockService;

    @Mock
    private OffsetDateTime timestamp;

    @Mock
    private MunicipalityService municipalityService;

    @BeforeEach
    void setUp() {

        accessibilityRequestMapper = new AccessibilityRequestMapper(
                transportTypeV2Mapper,
                emissionClassMapper,
                fuelTypeMapper,
                emissionZoneTypeMapper,
                clockService,
                municipalityService);
    }

    @Test
    void map_mapsFieldsCorrectly() {

        VehicleArguments vehicleArguments = VehicleArguments.builder()
                .vehicleAxleLoad(DEFAULT_VEHICLE_AXLE_LOAD)
                .vehicleHeight(DEFAULT_VEHICLE_HEIGHT)
                .vehicleLength(DEFAULT_VEHICLE_LENGTH)
                .vehicleWidth(DEFAULT_VEHICLE_WIDTH)
                .vehicleWeight(DEFAULT_VEHICLE_WEIGHT)
                .emissionClass(EmissionClassJson.EURO_1)
                .fuelTypes(List.of(FuelTypeJson.PETROL))
                .build();

        Excludes exemptions = Excludes.builder()
                .emissionZoneIds(Set.of("id1"))
                .emissionZoneTypes(Set.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE))
                .build();

        when(municipality.idAsInteger()).thenReturn(DEFAULT_MUNICIPALITY_ID);
        when(municipality.searchDistanceInMetres()).thenReturn((int) DEFAULT_SEARCH_DISTANCE);
        when(municipality.startCoordinateLatitude()).thenReturn(DEFAULT_LATITUDE_COORDINATE);
        when(municipality.startCoordinateLongitude()).thenReturn(DEFAULT_LONGITUDE_COORDINATE);
        when(municipality.bounds()).thenReturn(municipalityBoundingBox);

        when(municipalityBoundingBox.longitudeFrom()).thenReturn(DEFAULT_LONGITUDE_COORDINATE);
        when(municipalityBoundingBox.latitudeFrom()).thenReturn(DEFAULT_LATITUDE_COORDINATE);
        when(municipalityBoundingBox.longitudeTo()).thenReturn(MAX_LONGITUDE);
        when(municipalityBoundingBox.latitudeTo()).thenReturn(MAX_LATITUDE);

        when(transportTypeV2Mapper.map(vehicleArguments)).thenReturn(Set.of(TransportType.CAR));
        when(emissionClassMapper.map(EmissionClassJson.EURO_1)).thenReturn(Set.of(EmissionClass.EURO_1));
        when(fuelTypeMapper.map(FuelTypeJson.PETROL)).thenReturn(FuelType.PETROL);
        when(emissionZoneTypeMapper.map(EmissionZoneTypeJson.LOW_EMISSION_ZONE)).thenReturn(EmissionZoneType.LOW);

        when(clockService.now()).thenReturn(timestamp);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapper.map(
                municipality,
                vehicleArguments,
                exemptions,
                null,
                null);

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
                .excludeRestrictionsWithEmissionZoneIds(Set.of("id1"))
                .excludeRestrictionsWithEmissionZoneTypes(Set.of(EmissionZoneType.LOW))
                .build());
    }

    @Test
    void map_handlesNullValuesGracefully() {

        VehicleArguments vehicleArguments = VehicleArguments.builder().build();
        Excludes excludes = Excludes.builder().build();

        when(municipality.idAsInteger()).thenReturn(DEFAULT_MUNICIPALITY_ID);
        when(municipality.searchDistanceInMetres()).thenReturn((int) DEFAULT_SEARCH_DISTANCE);
        when(municipality.startCoordinateLatitude()).thenReturn(DEFAULT_LATITUDE_COORDINATE);
        when(municipality.startCoordinateLongitude()).thenReturn(DEFAULT_LONGITUDE_COORDINATE);
        when(municipality.bounds()).thenReturn(municipalityBoundingBox);

        when(municipalityBoundingBox.longitudeFrom()).thenReturn(DEFAULT_LONGITUDE_COORDINATE);
        when(municipalityBoundingBox.latitudeFrom()).thenReturn(DEFAULT_LATITUDE_COORDINATE);
        when(municipalityBoundingBox.longitudeTo()).thenReturn(MAX_LONGITUDE);
        when(municipalityBoundingBox.latitudeTo()).thenReturn(MAX_LATITUDE);

        when(transportTypeV2Mapper.map(vehicleArguments)).thenReturn(Set.of(TransportType.CAR));
        when(emissionClassMapper.map((EmissionClassJson) null)).thenReturn(null);

        when(clockService.now()).thenReturn(timestamp);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapper.map(
                municipality,
                vehicleArguments,
                excludes,
                null,
                null);

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
