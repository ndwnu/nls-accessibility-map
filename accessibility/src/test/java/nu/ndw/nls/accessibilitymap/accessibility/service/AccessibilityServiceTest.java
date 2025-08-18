package nu.ndw.nls.accessibilitymap.accessibility.service;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.EdgeRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityServiceTest {

    private static final double START_LOCATION_LATITUDE = 1d;

    private static final double START_LOCATION_LONGITUDE = 2d;

    private static final double END_LOCATION_LATITUDE = 3d;

    private static final double END_LOCATION_LONGITUDE = 4d;

    private static final long END_LOCATION_ROAD_SECTION_ID = 10;

    private static final int MUNICIPALITY_ID = 11;

    private static final double SEARCH_DISTANCE_IN_METRES = 200D;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @Mock
    private IsochroneServiceFactory isochroneServiceFactory;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue idIntEncodedValue;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    @Mock
    private RoadSectionMapper roadSectionMapper;

    @Mock
    private RoadSectionCombinator roadSectionCombinator;

    @Mock
    private ClockService clockService;

    @Mock
    private TrafficSign trafficSign;

    @Mock
    private IsochroneService isochroneService;

    @Mock
    private Snap startSegmentSnap;

    @Mock
    private Snap endSegmentSnap;

    @Mock
    private EdgeIteratorState endSegmentClosestEdge;

    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private Weighting weightingNoRestrictions;

    @Mock
    private RoadSection roadSectionNoRestriction;

    @Mock
    private RoadSection roadSectionRestriction;

    @Mock
    private RoadSection roadSectionCombined;

    @Mock
    private RoadSection roadSectionCombinedEnd;

    @Mock
    private RoadSection missingRoadSection;

    @Mock
    private IsochroneMatch isochroneMatchRestriction;

    @Mock
    private EdgeRestrictions edgeRestrictions;

    @Mock
    private NetworkData networkData;

    @Mock
    private NetworkCacheDataService networkCacheDataService;

    private AccessibilityService accessibilityService;

    @Mock
    private MissingRoadSectionProvider missingRoadSectionProvider;

    @Mock
    private AccessibilityReasonService accessibilityReasonService;

    @Mock
    private List<List<AccessibilityReason>> accessibilityReasons;

    @BeforeEach
    void setUp() {

        accessibilityService = new AccessibilityService(
                isochroneServiceFactory,
                trafficSignDataService,
                roadSectionMapper,
                clockService,
                networkCacheDataService,
                roadSectionCombinator,
                missingRoadSectionProvider,
                accessibilityReasonService);
    }

    @Test
    void calculateAccessibility() {

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                List.of(roadSectionNoRestriction),
                List.of(roadSectionRestriction)))
                .thenReturn(List.of(roadSectionCombined, roadSectionCombinedEnd));

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(START_LOCATION_LATITUDE)
                .startLocationLongitude(START_LOCATION_LONGITUDE)
                .endLocationLatitude(END_LOCATION_LATITUDE)
                .endLocationLongitude(END_LOCATION_LONGITUDE)
                .municipalityId(MUNICIPALITY_ID)
                .searchRadiusInMeters(SEARCH_DISTANCE_IN_METRES)
                .transportTypes(Set.of(TransportType.CAR))
                .build();

        prepareMocks(accessibilityRequest);

        Accessibility result = accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest);

        Accessibility expected = Accessibility
                .builder()
                .combinedAccessibility(List.of(roadSectionCombined, roadSectionCombinedEnd))
                .accessibleRoadsSectionsWithoutAppliedRestrictions(List.of(roadSectionNoRestriction))
                .accessibleRoadSectionsWithAppliedRestrictions(List.of(roadSectionRestriction))
                .toRoadSection(roadSectionCombinedEnd)
                .reasons(accessibilityReasons)
                .build();

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            null, null,
            1, null,
            null, 2
            """, nullValues = "null")
    void calculateAccessibility_noEndLocation(Double endLocationLatitude, Double endLocationLongitude) {

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                List.of(roadSectionNoRestriction),
                List.of(roadSectionRestriction)))
                .thenReturn(List.of(roadSectionCombined, roadSectionCombinedEnd));

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(START_LOCATION_LATITUDE)
                .startLocationLongitude(START_LOCATION_LONGITUDE)
                .endLocationLatitude(endLocationLatitude)
                .endLocationLongitude(endLocationLongitude)
                .municipalityId(MUNICIPALITY_ID)
                .searchRadiusInMeters(SEARCH_DISTANCE_IN_METRES)
                .transportTypes(Set.of(TransportType.CAR))
                .build();

        prepareMocks(accessibilityRequest);

        Accessibility result = accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest);

        Accessibility expected = Accessibility
                .builder()
                .combinedAccessibility(List.of(roadSectionCombined, roadSectionCombinedEnd))
                .accessibleRoadsSectionsWithoutAppliedRestrictions(List.of(roadSectionNoRestriction))
                .accessibleRoadSectionsWithAppliedRestrictions(List.of(roadSectionRestriction))
                .toRoadSection(null)
                .reasons(Collections.emptyList())
                .build();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void calculateAccessibility_addMissingRoadSectionsFromNwb() {

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                List.of(roadSectionNoRestriction, missingRoadSection),
                List.of(roadSectionRestriction, missingRoadSection)))
                .thenReturn(List.of(roadSectionCombined, missingRoadSection, roadSectionCombinedEnd));

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .addMissingRoadsSectionsFromNwb(true)
                .startLocationLatitude(START_LOCATION_LATITUDE)
                .startLocationLongitude(START_LOCATION_LONGITUDE)
                .endLocationLatitude(END_LOCATION_LATITUDE)
                .endLocationLongitude(END_LOCATION_LONGITUDE)
                .municipalityId(MUNICIPALITY_ID)
                .searchRadiusInMeters(SEARCH_DISTANCE_IN_METRES)
                .transportTypes(Set.of(TransportType.CAR))
                .build();

        prepareMocks(accessibilityRequest);

        when(missingRoadSectionProvider.get(MUNICIPALITY_ID, List.of(roadSectionNoRestriction), false))
                .thenReturn(List.of(missingRoadSection));

        Accessibility result = accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest);

        Accessibility expected = Accessibility
                .builder()
                .combinedAccessibility(List.of(roadSectionCombined, missingRoadSection, roadSectionCombinedEnd))
                .accessibleRoadsSectionsWithoutAppliedRestrictions(List.of(roadSectionNoRestriction, missingRoadSection))
                .accessibleRoadSectionsWithAppliedRestrictions(List.of(roadSectionRestriction, missingRoadSection))
                .toRoadSection(roadSectionCombinedEnd)
                .reasons(accessibilityReasons)
                .build();

        assertThat(result).isEqualTo(expected);
    }

    private void prepareMocks(AccessibilityRequest accessibilityRequest) {

        when(clockService.now()).thenReturn(OffsetDateTime.MIN);

        when(locationIndexTree.findClosest(
                START_LOCATION_LATITUDE,
                START_LOCATION_LONGITUDE,
                EdgeFilter.ALL_EDGES))
                .thenReturn(startSegmentSnap);

        if (Objects.nonNull(accessibilityRequest.endLocationLatitude()) && Objects.nonNull(accessibilityRequest.endLocationLongitude())) {
            when(locationIndexTree.findClosest(
                    END_LOCATION_LATITUDE,
                    END_LOCATION_LONGITUDE,
                    EdgeFilter.ALL_EDGES))
                    .thenReturn(endSegmentSnap);
            when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
            when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(idIntEncodedValue);
            when(endSegmentSnap.getClosestEdge()).thenReturn(endSegmentClosestEdge);
            when(endSegmentClosestEdge.get(idIntEncodedValue)).thenReturn((int) END_LOCATION_ROAD_SECTION_ID);

            when(roadSectionCombinedEnd.getId()).thenReturn(END_LOCATION_ROAD_SECTION_ID);
            when(roadSectionCombinedEnd.isRestrictedInAnyDirection()).thenReturn(true);

            when(accessibilityReasonService.calculateReasons(accessibilityRequest, networkData, List.of(trafficSign)))
                    .thenReturn(accessibilityReasons);
        }

        when(trafficSignDataService.findAllBy(accessibilityRequest)).thenReturn(List.of(trafficSign));

        when(networkCacheDataService.getNetworkData(
                MUNICIPALITY_ID,
                startSegmentSnap,
                SEARCH_DISTANCE_IN_METRES,
                List.of(trafficSign),
                networkGraphHopper)
        ).thenReturn(networkData);

        when(isochroneServiceFactory.createService(networkGraphHopper)).thenReturn(isochroneService);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(networkGraphHopper.createWeighting(
                eq(NetworkConstants.CAR_PROFILE),
                argThat(new PMapArgumentMatcher(new PMap())))
        ).thenReturn(weightingNoRestrictions);

        when(networkData.networkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkData.queryGraph()).thenReturn(queryGraph);
        when(networkData.edgeRestrictions()).thenReturn(edgeRestrictions);
        when(edgeRestrictions.getBlockedEdges()).thenReturn(Set.of(1));
        when(isochroneService
                .getIsochroneMatchesByMunicipalityId(
                        argThat(new IsochroneArgumentMatcher(IsochroneArguments.builder()
                                .weighting(new RestrictionWeightingAdapter(weightingNoRestrictions, edgeRestrictions.getBlockedEdges()))
                                .municipalityId(MUNICIPALITY_ID)
                                .searchDistanceInMetres(SEARCH_DISTANCE_IN_METRES)
                                .build())),
                        eq(queryGraph),
                        eq(startSegmentSnap)))
                .thenReturn(List.of(isochroneMatchRestriction));

        when(networkData.baseAccessibleRoads())
                .thenReturn(new ArrayList<>(List.of(roadSectionNoRestriction)));
        when(roadSectionMapper.mapToRoadSections(List.of(isochroneMatchRestriction)))
                .thenReturn(new ArrayList<>(List.of(roadSectionRestriction)));
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
            // Mockito initializes with a null value
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
            // Mockito initializes with a null value
            if (actual == null) {
                return false;
            }
            return Objects.equals(expected.municipalityId(), actual.municipalityId()) &&
                    expected.searchDistanceInMetres() == actual.searchDistanceInMetres() &&
                    weightingEquals(expected.weighting(), actual.weighting());
        }

        private boolean weightingEquals(Weighting expectedWeighting, Weighting actualWeighting) {

            if (expected.weighting() instanceof RestrictionWeightingAdapter expectedWeightingAdapter
                    && actualWeighting instanceof RestrictionWeightingAdapter actualWeightingAdapter) {
                return Objects.equals(
                        expectedWeightingAdapter.getBlockedEdges(),
                        actualWeightingAdapter.getBlockedEdges());
            } else {
                return Objects.equals(expectedWeighting, actualWeighting);
            }
        }
    }
}
