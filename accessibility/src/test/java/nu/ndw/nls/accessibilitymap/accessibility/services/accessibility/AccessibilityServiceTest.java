package nu.ndw.nls.accessibilitymap.accessibility.services.accessibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphFactory;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.mappers.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.mappers.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityServiceTest {

    private static final double START_LOCATION_LATITUDE = 1d;

    private static final double START_LOCATION_LONGITUDE = 2d;

    private static final int MUNICIPALITY_ID = 11;

    private static final int TRAFFIC_SIGN_ID = 345;

    private static final double SEARCH_DISTANCE_IN_METRES = 200D;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @Mock
    private IsochroneServiceFactory isochroneServiceFactory;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    @Mock
    private GeometryFactoryWgs84 geometryFactoryWgs84;

    @Mock
    private RoadSectionMapper roadSectionMapper;

    @Mock
    private RoadSectionCombinator roadSectionCombinator;

    @Mock
    private ClockService clockService;

    @Mock
    private TrafficSignSnapMapper trafficSingSnapMapper;

    @Mock
    private QueryGraphFactory queryGraphFactory;

    @Mock
    QueryGraphConfigurer queryGraphConfigurer;

    @Mock
    private TrafficSign trafficSign;

    @Mock
    private IsochroneService isochroneService;

    @Mock
    private Snap startSegmentSnap;

    @Mock
    private Point startPoint;

    @Mock
    private TrafficSignSnap trafficSignSnap;

    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private Profile profile;

    @Mock
    private Weighting weightingNoRestrictions;

    @Mock
    private Weighting weightingRestrictions;

    @Mock
    private RoadSection roadSectionNoRestriction;

    @Mock
    private RoadSection roadSectionRestriction;

    @Mock
    private RoadSection roadSectionCombined;

    @Mock
    private IsochroneMatch isochroneMatchNoRestriction;

    @Mock
    private IsochroneMatch isochroneMatchRestriction;

    @Captor
    private ArgumentCaptor<Coordinate> coordinateArgumentCaptor;

    private AccessibilityService accessibilityService;

    @BeforeEach
    void setUp() {

        accessibilityService = new AccessibilityService(isochroneServiceFactory, networkGraphHopper,
                trafficSignDataService, geometryFactoryWgs84, roadSectionMapper, roadSectionCombinator,
                clockService, trafficSingSnapMapper, queryGraphFactory, queryGraphConfigurer);
    }

    @Test
    void calculateAccessibility() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.MIN)
                .thenReturn(OffsetDateTime.MIN.plusMinutes(1).plusNanos(1000));

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(START_LOCATION_LATITUDE)
                .startLocationLongitude(START_LOCATION_LONGITUDE)
                .municipalityId(MUNICIPALITY_ID)
                .searchRadiusInMeters(SEARCH_DISTANCE_IN_METRES)
                .transportTypes(Set.of(TransportType.CAR))
                .build();

        mockTrafficSignData(accessibilityRequest);
        mockWeighting();

        when(startPoint.getX()).thenReturn(START_LOCATION_LONGITUDE);
        when(startPoint.getY()).thenReturn(START_LOCATION_LATITUDE);
        when(isochroneServiceFactory.createService(networkGraphHopper)).thenReturn(isochroneService);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(locationIndexTree.findClosest(START_LOCATION_LATITUDE, START_LOCATION_LONGITUDE,
                EdgeFilter.ALL_EDGES)).thenReturn(startSegmentSnap);
        when(trafficSingSnapMapper.map(List.of(trafficSign),
                true))
                .thenReturn(List.of(trafficSignSnap));
        when(geometryFactoryWgs84.createPoint(coordinateArgumentCaptor.capture()))
                .thenReturn(startPoint);
        when(queryGraphFactory.createQueryGraphWithoutConfig(List.of(trafficSignSnap), startSegmentSnap))
                .thenReturn(queryGraph);

        when(isochroneService
                .getIsochroneMatchesByMunicipalityId(
                        argThat(new IsochroneArgumentMatcher(IsochroneArguments
                                .builder()
                                .weighting(weightingNoRestrictions)
                                .startPoint(startPoint)
                                .municipalityId(MUNICIPALITY_ID)
                                .searchDistanceInMetres(SEARCH_DISTANCE_IN_METRES)
                                .build())),
                        eq(queryGraph),
                        eq(startSegmentSnap)))
                .thenReturn(List.of(isochroneMatchNoRestriction));

        when(isochroneService
                .getIsochroneMatchesByMunicipalityId(
                        argThat(new IsochroneArgumentMatcher(IsochroneArguments
                                        .builder()
                                        .weighting(weightingRestrictions)
                                        .startPoint(startPoint)
                                        .municipalityId(MUNICIPALITY_ID)
                                        .searchDistanceInMetres(SEARCH_DISTANCE_IN_METRES)
                                        .build()
                                )
                        ),
                        eq(queryGraph),
                        eq(startSegmentSnap)))
                .thenReturn(List.of(isochroneMatchRestriction));

        when(roadSectionMapper.mapToRoadSections(List.of(isochroneMatchNoRestriction),
                Map.of(TRAFFIC_SIGN_ID, trafficSign)))
                .thenReturn(List.of(roadSectionNoRestriction));
        when(roadSectionMapper.mapToRoadSections(List.of(isochroneMatchRestriction),
                Map.of(TRAFFIC_SIGN_ID, trafficSign)))
                .thenReturn(List.of(roadSectionRestriction));
        when(roadSectionCombinator
                .combineNoRestrictionsWithAccessibilityRestrictions(
                        List.of(roadSectionNoRestriction),
                        List.of(roadSectionRestriction)))
                .thenReturn(List.of(roadSectionCombined));

        Accessibility result = accessibilityService.calculateAccessibility(accessibilityRequest, true);

        Accessibility expected = Accessibility
                .builder()
                .combinedAccessibility(List.of(roadSectionCombined))
                .accessibleRoadsSectionsWithoutAppliedRestrictions(List.of(roadSectionNoRestriction))
                .accessibleRoadSectionsWithAppliedRestrictions(List.of(roadSectionRestriction))
                .build();

        assertThat(result).isEqualTo(expected);
        Coordinate startCoordinate = coordinateArgumentCaptor.getValue();
        assertThat(startCoordinate.getX()).isEqualTo(START_LOCATION_LONGITUDE);
        assertThat(startCoordinate.getY()).isEqualTo(START_LOCATION_LATITUDE);
    }

    private void mockWeighting() {

        when(networkGraphHopper
                .getProfile(NetworkConstants.VEHICLE_NAME_CAR))
                .thenReturn(profile);

        when(networkGraphHopper.createWeighting(eq(profile), argThat(new PMapArgumentMatcher(new PMap())))).thenReturn(
                weightingNoRestrictions);
    }

    private void mockTrafficSignData(AccessibilityRequest accessibilityRequest) {

        when(trafficSignDataService.findAllBy(accessibilityRequest)).thenReturn(List.of(trafficSign));
        when(trafficSign.id()).thenReturn(TRAFFIC_SIGN_ID);
        when(trafficSignSnap.getTrafficSign()).thenReturn(trafficSign);
    }

    @Test
    void annotation_calculateAccessibility() {

        AnnotationUtil.methodContainsAnnotation(
                accessibilityService.getClass(),
                Timed.class,
                "calculateAccessibility",
                annotation -> assertThat(annotation.description())
                        .isEqualTo("Time spent calculating accessibility")
        );
    }

    private record PMapArgumentMatcher(PMap expected) implements ArgumentMatcher<PMap> {

        @Override
        public boolean matches(PMap actual) {
            // Mockito initializes with null value
            if (actual == null) {
                return false;
            }

            return expected.toMap().equals(actual.toMap());
        }
    }

    private record IsochroneArgumentMatcher(IsochroneArguments expected) implements
            ArgumentMatcher<IsochroneArguments> {

        @Override
        public boolean matches(IsochroneArguments actual) {
            // Mockito initializes with null value
            if (actual == null) {
                return false;
            }

            return expected.equals(actual);
        }
    }
}
