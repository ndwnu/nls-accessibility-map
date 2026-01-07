package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.Excludes;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response.RoadSectionFeatureCollectionMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.validator.PointValidator;
import nu.ndw.nls.accessibilitymap.backend.exception.IncompleteArgumentsException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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

    private static final long REQUESTED_ROAD_SECTION_ID = 123;

    private static final double REQUESTED_LONGITUDE = 3333;

    private static final double REQUESTED_LATITUDE = 222;

    private static final String ENVIRONMENTAL_ZONE_PARAMETER_ERROR_MESSAGE = "If one of the environmental zone parameters is set, the other must be set as well.";

    @Mock
    private GraphHopperService graphHopperService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private PointValidator pointValidator;

    @Mock
    private AccessibilityResponseMapper accessibilityResponseMapper;

    @Mock
    private RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    @Mock
    private AccessibilityRequestMapper accessibilityRequestMapper;

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
    private Collection<RoadSection> roadSections;

    @BeforeEach
    void setup() {

        accessibilityMapApiDelegate = new AccessibilityMapApiDelegateImpl(
                pointValidator,
                graphHopperService,
                accessibilityResponseMapper,
                roadSectionFeatureCollectionMapper,
                municipalityService,
                accessibilityRequestMapper,
                accessibilityService);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectEmissionZoneParameters")
    @SuppressWarnings("java:S5778")
    void getInaccessibleRoadSections_shouldThrowIncompleteArgumentsException(
            EmissionClassJson emissionClassJson,
            List<FuelTypeJson> fuelTypesJson) {

        assertThatThrownBy(() -> accessibilityMapApiDelegate.getInaccessibleRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR, VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                REQUESTED_LATITUDE, REQUESTED_LONGITUDE,
                emissionClassJson,
                fuelTypesJson,
                List.of("id1"), List.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE)
        ))
                .isExactlyInstanceOf(IncompleteArgumentsException.class)
                .hasMessageContaining(ENVIRONMENTAL_ZONE_PARAMETER_ERROR_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEmissionZoneParameters")
    void getInaccessibleRoadSections(EmissionClassJson emissionClassJson, List<FuelTypeJson> fuelTypesJson) {

        setUpFixture(emissionClassJson, fuelTypesJson);

        when(accessibilityResponseMapper.map(accessibility)).thenReturn(accessibilityMapResponseJson);

        ResponseEntity<AccessibilityMapResponseJson> response = accessibilityMapApiDelegate.getInaccessibleRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR, VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                REQUESTED_LATITUDE, REQUESTED_LONGITUDE,
                emissionClassJson, fuelTypesJson,
                List.of("id1"), List.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(accessibilityMapResponseJson);

        verify(pointValidator).validateConsistentValues(REQUESTED_LATITUDE, REQUESTED_LONGITUDE);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectEmissionZoneParameters")
    @SuppressWarnings("java:S5778")
    void getRoadSections_shouldThrowIncompleteArgumentsException(EmissionClassJson emissionClassJson, List<FuelTypeJson> fuelTypesJson) {

        assertThatThrownBy(() -> accessibilityMapApiDelegate.getRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR, VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                true,
                REQUESTED_LATITUDE, REQUESTED_LONGITUDE,
                emissionClassJson, fuelTypesJson,
                List.of("id1"), List.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE)))
                .isExactlyInstanceOf(IncompleteArgumentsException.class)
                .hasMessageContaining(ENVIRONMENTAL_ZONE_PARAMETER_ERROR_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEmissionZoneParameters")
    void getRoadSections(EmissionClassJson emissionClassJson, List<FuelTypeJson> fuelTypesJson) {

        setUpFixture(emissionClassJson, fuelTypesJson);
        when(accessibilityRequest.hasEndLocation()).thenReturn(true);
        when(accessibility.toRoadSection()).thenReturn(Optional.of(RoadSection.builder().id(REQUESTED_ROAD_SECTION_ID).build()));
        when(accessibility.combinedAccessibility()).thenReturn(roadSections);

        when(roadSectionFeatureCollectionMapper
                .map(roadSections, true, REQUESTED_ROAD_SECTION_ID, true))
                .thenReturn(roadSectionFeatureCollectionJson);

        ResponseEntity<RoadSectionFeatureCollectionJson> response = accessibilityMapApiDelegate.getRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR,
                VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                true,
                REQUESTED_LATITUDE, REQUESTED_LONGITUDE,
                emissionClassJson, fuelTypesJson,
                List.of("id1"), List.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roadSectionFeatureCollectionJson);

        verify(pointValidator).validateConsistentValues(REQUESTED_LATITUDE, REQUESTED_LONGITUDE);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEmissionZoneParameters")
    void getRoadSections_noEndRoadSectionFound(EmissionClassJson emissionClassJson, List<FuelTypeJson> fuelTypesJson) {

        setUpFixture(emissionClassJson, fuelTypesJson);
        when(accessibilityRequest.hasEndLocation()).thenReturn(true);
        when(accessibility.toRoadSection()).thenReturn(Optional.empty());
        when(accessibility.combinedAccessibility()).thenReturn(roadSections);

        when(roadSectionFeatureCollectionMapper
                .map(roadSections, true, null, true))
                .thenReturn(roadSectionFeatureCollectionJson);

        ResponseEntity<RoadSectionFeatureCollectionJson> response = accessibilityMapApiDelegate.getRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR,
                VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                true,
                REQUESTED_LATITUDE, REQUESTED_LONGITUDE,
                emissionClassJson, fuelTypesJson,
                List.of("id1"), List.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roadSectionFeatureCollectionJson);

        verify(pointValidator).validateConsistentValues(REQUESTED_LATITUDE, REQUESTED_LONGITUDE);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            true, true,
            false, true,
            true, false,
            false, false
            """)
    void getRoadSections_noEndLocation(boolean hasRequestedLatitude, boolean hasRequestedLongitude) {

        boolean hasValidStartPoint = (hasRequestedLatitude && hasRequestedLongitude) || !hasRequestedLatitude && !hasRequestedLongitude;

        if (hasValidStartPoint) {
            setUpFixture(EmissionClassJson.EURO_1, List.of(FuelTypeJson.ETHANOL));
            when(accessibilityRequest.hasEndLocation()).thenReturn(true);
            when(accessibilityRequest.hasEndLocation()).thenReturn(true);
            when(accessibility.toRoadSection()).thenReturn(Optional.of(RoadSection.builder().id(REQUESTED_ROAD_SECTION_ID).build()));
            when(accessibility.combinedAccessibility()).thenReturn(roadSections);
            when(roadSectionFeatureCollectionMapper.map(roadSections, true, REQUESTED_ROAD_SECTION_ID, true))
                    .thenReturn(roadSectionFeatureCollectionJson);

            ResponseEntity<RoadSectionFeatureCollectionJson> response = accessibilityMapApiDelegate.getRoadSections(
                    MUNICIPALITY_ID,
                    VehicleTypeJson.CAR,
                    VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                    true,
                    REQUESTED_LATITUDE,
                    REQUESTED_LONGITUDE,
                    EmissionClassJson.EURO_1, List.of(FuelTypeJson.ETHANOL),
                    List.of("id1"), List.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(roadSectionFeatureCollectionJson);
        } else {
            doThrow(new RuntimeException("error")).when(pointValidator).validateConsistentValues(
                    hasRequestedLatitude ? REQUESTED_LATITUDE : null,
                    hasRequestedLongitude ? REQUESTED_LONGITUDE : null
            );
            assertThat(catchThrowable(() -> accessibilityMapApiDelegate.getRoadSections(
                    MUNICIPALITY_ID,
                    VehicleTypeJson.CAR,
                    VEHICLE_LENGTH, VEHICLE_WIDTH, VEHICLE_HEIGHT, VEHICLE_WEIGHT, VEHICLE_AXLE_LOAD, false,
                    true,
                    hasRequestedLatitude ? REQUESTED_LATITUDE : null,
                    hasRequestedLongitude ? REQUESTED_LONGITUDE : null,
                    EmissionClassJson.EURO_1, List.of(FuelTypeJson.ETHANOL),
                    List.of("id1"), List.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE))))
                    .hasMessage("error")
                    .isInstanceOf(RuntimeException.class);
        }
    }

    private void setUpFixture(EmissionClassJson emissionClassJson, List<FuelTypeJson> fuelTypesJson) {

        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        when(accessibilityRequestMapper.mapToAccessibilityRequest(
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
                        .fuelTypes(fuelTypesJson)
                        .build(),
                Excludes.builder()
                        .emissionZoneIds(Set.of("id1"))
                        .emissionZoneTypes(Set.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE))
                        .build(),
                REQUESTED_LATITUDE,
                REQUESTED_LONGITUDE))
                .thenReturn(accessibilityRequest);

        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID)).thenReturn(municipality);
    }

    static Stream<Arguments> provideIncorrectEmissionZoneParameters() {

        return Stream.of(
                Arguments.of(EmissionClassJson.EURO_5, null),
                Arguments.of(null, List.of(FuelTypeJson.PETROL))
        );
    }

    static Stream<Arguments> provideCorrectEmissionZoneParameters() {

        return Stream.of(
                Arguments.of(EmissionClassJson.EURO_5, List.of(FuelTypeJson.PETROL)),
                Arguments.of(null, null)
        );
    }
}
