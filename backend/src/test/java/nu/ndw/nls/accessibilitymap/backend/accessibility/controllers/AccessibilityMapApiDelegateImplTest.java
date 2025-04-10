package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibleRoadSectionModifier;
import nu.ndw.nls.accessibilitymap.accessibility.services.MissingRoadSectionProvider;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response.PointMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response.RoadSectionFeatureCollectionMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.validators.PointValidator;
import nu.ndw.nls.accessibilitymap.backend.accessibility.service.PointMatchService;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.services.MunicipalityService;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
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
    private static final int REQUESTED_ROAD_SECTION_ID = 123;
    private static final double REQUESTED_LONGITUDE = 3333;
    private static final double REQUESTED_LATITUDE = 222;
    private static final int MUNICIPALITY_ID_INTEGER = 123;
    private static final double SEARCH_DISTANCE_IN_METER = 1000D;

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
    private CandidateMatch candidateMatch;
    @Mock
    private AccessibilityMapResponseJson accessibilityMapResponseJson;
    @Mock
    private RoadSectionFeatureCollectionJson roadSectionFeatureCollectionJson;
    @Mock
    private MunicipalityService municipalityService;
    @Mock
    private AccessibilityService accessibilityService;
    @Mock
    private MissingRoadSectionProvider missingRoadSectionProvider;

    private AccessibilityMapApiDelegateImpl accessibilityMapApiDelegate;

    @Mock
    private Municipality municipality;
    @Mock
    private Point startPoint;
    @Mock
    private AccessibilityRequest accessibilityRequest;
    @Mock
    private Accessibility accessibility;

    @BeforeEach
    void setup() {
        accessibilityMapApiDelegate = new AccessibilityMapApiDelegateImpl(pointValidator,
                pointMapper,
                pointMatchService,
                accessibilityResponseMapper,
                roadSectionFeatureCollectionMapper,
                municipalityService,
                accessibilityRequestMapper,
                accessibilityService, missingRoadSectionProvider);
    }

    @Test
    void getInaccessibleRoadSections() {
        setUpFixture();
        when(accessibilityResponseMapper.map(accessibility, REQUESTED_ROAD_SECTION_ID))
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(accessibilityMapResponseJson);

        verify(pointValidator).validateConsistentValues(REQUESTED_LATITUDE, REQUESTED_LONGITUDE);
    }

    @Test
    void getRoadSections() {
        setUpFixture();
        when(roadSectionFeatureCollectionMapper
                .map(accessibility, true, candidateMatch, true))
                .thenReturn(roadSectionFeatureCollectionJson);
        ResponseEntity<RoadSectionFeatureCollectionJson> response = accessibilityMapApiDelegate.getRoadSections(
                MUNICIPALITY_ID,
                VehicleTypeJson.CAR,
                VEHICLE_LENGTH,
                VEHICLE_WIDTH,
                VEHICLE_HEIGHT,
                VEHICLE_WEIGHT,
                VEHICLE_AXLE_LOAD,
                false, true, REQUESTED_LATITUDE, REQUESTED_LONGITUDE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roadSectionFeatureCollectionJson);

        verify(pointValidator).validateConsistentValues(REQUESTED_LATITUDE, REQUESTED_LONGITUDE);
    }

    private void setUpFixture() {

        when(accessibilityRequestMapper.mapToAccessibilityRequest(municipality, VehicleArguments.builder()
                .vehicleType(VehicleTypeJson.CAR)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(VEHICLE_WEIGHT)
                .vehicleAxleLoad(VEHICLE_AXLE_LOAD)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHasTrailer(false)
                .build()))
                .thenReturn(accessibilityRequest);
        when(pointMapper.mapCoordinate(REQUESTED_LATITUDE, REQUESTED_LONGITUDE)).thenReturn(Optional.of(requestedPoint));
        when(pointMatchService.match(requestedPoint)).thenReturn(Optional.of(candidateMatch));
        when(candidateMatch.getMatchedLinkId()).thenReturn(REQUESTED_ROAD_SECTION_ID);
        when(accessibilityService.calculateAccessibility(eq(accessibilityRequest),
                any(AccessibleRoadSectionModifier.class)))
                .thenReturn(accessibility);
        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID)).thenReturn(municipality);
    }
}
