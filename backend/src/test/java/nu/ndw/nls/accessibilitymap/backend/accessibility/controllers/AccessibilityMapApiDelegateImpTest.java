package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response.PointMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response.RoadSectionFeatureCollectionMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.validator.PointValidator;
import nu.ndw.nls.accessibilitymap.backend.accessibility.service.PointMatchService;
import nu.ndw.nls.accessibilitymap.backend.exception.IncompleteArgumentsException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapApiDelegateImpTest {

    private static final float VEHICLE_LENGTH = 1F;

    private static final float VEHICLE_WIDTH = 2F;

    private static final float VEHICLE_HEIGHT = 3F;

    private static final float VEHICLE_WEIGHT = 4F;

    private static final float VEHICLE_AXLE_LOAD = 5F;

    private static final String MUNICIPALITY_ID = "GM0344";

    private static final int REQUESTED_ROAD_SECTION_ID = 123;

    private static final double REQUESTED_LONGITUDE = 3333;

    private static final double REQUESTED_LATITUDE = 222;

    private static final int MUNICIPALITY_ID_INTEGER = 123;

    private static final String ENVIRONMENTAL_ZONE_PARAMETER_ERROR_MESSAGE = "If one of the environmental zone parameters is set, the other must be set as well.";

    @Mock
    private GraphHopperService graphHopperService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private PointValidator pointValidator;

    @Mock
    private PointMapper pointMapper;

    @Mock
    private PointMatchService pointMatchService;

    @Mock
    private AccessibilityResponseMapper accessibilityResponseMapper;

    @Mock
    private RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    @Mock
    private AccessibilityRequestMapper accessibilityRequestMapper;

    @Mock
    private Point requestedPoint;

    @Mock
    private CandidateMatch startPoint;

    @Mock
    private AccessibilityMapResponseJson accessibilityMapResponseJson;

    @Mock
    private RoadSectionFeatureCollectionJson roadSectionFeatureCollectionJson;

    @Mock
    private MunicipalityService municipalityService;

    @Mock
    private AccessibilityService accessibilityService;

    private AccessibilityMapApiDelegateImpl accessibilityMapApiDelegate;

    @Mock
    private Municipality municipality;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private Accessibility accessibility;

    @Mock
    private ClockService clockService;

    @Mock
    private OffsetDateTime timestamp;

    @Mock
    private Collection<RoadSection> roadSections;

    @BeforeEach
    void setup() {

        accessibilityMapApiDelegate = new AccessibilityMapApiDelegateImpl(pointValidator,
                pointMapper,
                graphHopperService,
                pointMatchService,
                accessibilityResponseMapper,
                roadSectionFeatureCollectionMapper,
                municipalityService,
                accessibilityRequestMapper,
                accessibilityService,
                clockService);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectEmissionZoneParameters")
    void getInaccessibleRoadSections_shouldThrowIncompleteArgumentsException(
            EmissionClassJson emissionClassJson,
            FuelTypeJson fuelTypeJson) {

        assertThatThrownBy(() -> accessibilityMapApiDelegate.getInaccessibleRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR,
                VEHICLE_LENGTH,
                VEHICLE_WIDTH,
                VEHICLE_HEIGHT,
                VEHICLE_WEIGHT,
                VEHICLE_AXLE_LOAD,
                false, REQUESTED_LATITUDE, REQUESTED_LONGITUDE, emissionClassJson,
                fuelTypeJson))
                .isExactlyInstanceOf(IncompleteArgumentsException.class)
                .hasMessageContaining(ENVIRONMENTAL_ZONE_PARAMETER_ERROR_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEmissionZoneParameters")
    void getInaccessibleRoadSections(EmissionClassJson emissionClassJson, FuelTypeJson fuelTypeJson) {

        setUpFixture(emissionClassJson, fuelTypeJson);

        when(pointMapper.mapCoordinate(REQUESTED_LATITUDE, REQUESTED_LONGITUDE)).thenReturn(Optional.of(requestedPoint));
        when(pointMatchService.match(networkGraphHopper, requestedPoint)).thenReturn(Optional.of(startPoint));
        when(startPoint.getMatchedLinkId()).thenReturn(REQUESTED_ROAD_SECTION_ID);

        when(accessibilityResponseMapper.map(accessibility, REQUESTED_ROAD_SECTION_ID))
                .thenReturn(accessibilityMapResponseJson);

        ResponseEntity<AccessibilityMapResponseJson> response = accessibilityMapApiDelegate.getInaccessibleRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR, VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                REQUESTED_LATITUDE, REQUESTED_LONGITUDE,
                emissionClassJson, fuelTypeJson);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(accessibilityMapResponseJson);

        verify(pointValidator).validateConsistentValues(REQUESTED_LATITUDE, REQUESTED_LONGITUDE);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectEmissionZoneParameters")
    void getRoadSections_shouldThrowIncompleteArgumentsException(EmissionClassJson emissionClassJson, FuelTypeJson fuelTypeJson) {
        assertThatThrownBy(() -> accessibilityMapApiDelegate.getRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR, VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                true,
                REQUESTED_LATITUDE, REQUESTED_LONGITUDE,
                emissionClassJson, fuelTypeJson))
                .isExactlyInstanceOf(IncompleteArgumentsException.class)
                .hasMessageContaining(ENVIRONMENTAL_ZONE_PARAMETER_ERROR_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEmissionZoneParameters")
    void getRoadSections(EmissionClassJson emissionClassJson, FuelTypeJson fuelTypeJson) {

        setUpFixture(emissionClassJson, fuelTypeJson);
        when(pointMapper.mapCoordinate(REQUESTED_LATITUDE, REQUESTED_LONGITUDE)).thenReturn(Optional.of(requestedPoint));
        when(pointMatchService.match(networkGraphHopper, requestedPoint)).thenReturn(Optional.of(startPoint));
        when(startPoint.getMatchedLinkId()).thenReturn(REQUESTED_ROAD_SECTION_ID);
        when(accessibility.combinedAccessibility()).thenReturn(roadSections);

        when(roadSectionFeatureCollectionMapper
                .map(roadSections, true, (long) REQUESTED_ROAD_SECTION_ID, true))
                .thenReturn(roadSectionFeatureCollectionJson);

        ResponseEntity<RoadSectionFeatureCollectionJson> response = accessibilityMapApiDelegate.getRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR,
                VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                true,
                REQUESTED_LATITUDE, REQUESTED_LONGITUDE,
                emissionClassJson, fuelTypeJson);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roadSectionFeatureCollectionJson);

        verify(pointValidator).validateConsistentValues(REQUESTED_LATITUDE, REQUESTED_LONGITUDE);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            1,      2,
            null,   2,
            1,      null,
            null,   null
            """)
    void getRoadSections_noStartLocation(Double requestedLatitude, Double requestedLongitude) {

        setUpFixture(EmissionClassJson.EURO_1, FuelTypeJson.ETHANOL);

        boolean expectStartPoint = Objects.nonNull(requestedLatitude) && Objects.nonNull(requestedLongitude);

        if (expectStartPoint) {
            when(pointMapper.mapCoordinate(requestedLatitude, requestedLongitude)).thenReturn(Optional.of(requestedPoint));
            when(pointMatchService.match(networkGraphHopper, requestedPoint)).thenReturn(Optional.of(startPoint));
            when(startPoint.getMatchedLinkId()).thenReturn(REQUESTED_ROAD_SECTION_ID);
        }
        when(accessibility.combinedAccessibility()).thenReturn(roadSections);

        when(roadSectionFeatureCollectionMapper
                .map(roadSections,
                        expectStartPoint,
                        expectStartPoint ? (long) REQUESTED_ROAD_SECTION_ID : null,
                        true))
                .thenReturn(roadSectionFeatureCollectionJson);

        ResponseEntity<RoadSectionFeatureCollectionJson> response = accessibilityMapApiDelegate.getRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR,
                VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                true,
                requestedLatitude, requestedLongitude,
                EmissionClassJson.EURO_1, FuelTypeJson.ETHANOL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roadSectionFeatureCollectionJson);

        if (expectStartPoint) {
            verify(pointValidator).validateConsistentValues(requestedLatitude, requestedLongitude);
        }
    }

    private void setUpFixture(EmissionClassJson emissionClassJson, FuelTypeJson fuelTypeJson) {

        when(clockService.now()).thenReturn(timestamp);
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        when(accessibilityRequestMapper.mapToAccessibilityRequest(
                timestamp,
                municipality,
                VehicleArguments.builder()
                        .vehicleType(VehicleTypeJson.CAR)
                        .vehicleHeight(VEHICLE_HEIGHT)
                        .vehicleLength(VEHICLE_LENGTH)
                        .vehicleWeight(VEHICLE_WEIGHT)
                        .vehicleAxleLoad(VEHICLE_AXLE_LOAD)
                        .vehicleWidth(VEHICLE_WIDTH)
                        .vehicleHasTrailer(false)
                        .emissionClass(emissionClassJson)
                        .fuelType(fuelTypeJson)
                        .build()))
                .thenReturn(accessibilityRequest);

        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID)).thenReturn(municipality);
    }

    static Stream<Arguments> provideIncorrectEmissionZoneParameters() {
        return Stream.of(
                Arguments.of(EmissionClassJson.EURO_5, null),
                Arguments.of(null, FuelTypeJson.PETROL)
        );
    }

    static Stream<Arguments> provideCorrectEmissionZoneParameters() {
        return Stream.of(
                Arguments.of(EmissionClassJson.EURO_5, FuelTypeJson.PETROL),
                Arguments.of(null, null)
        );
    }
}
