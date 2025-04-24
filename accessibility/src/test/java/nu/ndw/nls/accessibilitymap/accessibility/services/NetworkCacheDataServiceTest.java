package nu.ndw.nls.accessibilitymap.accessibility.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkCacheDataServiceTest {

    private static final String TRAFFIC_SIGN_ID_1 = "1";

    private static final String TRAFFIC_SIGN_ID_2 = "2";
    private static final int MUNICIPALITY_ID = 456;
    private static final double SEARCH_RADIUS_IN_METERS = 1000.0;

    @Mock
    private QueryGraphFactory queryGraphFactory;

    @Mock
    private TrafficSignSnapMapper trafficSignSnapMapper;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private IsochroneServiceFactory isochroneServiceFactory;

    @Mock
    private RoadSectionMapper roadSectionMapper;

    @Mock
    private RoadSectionTrafficSignAssigner roadSectionTrafficSignAssigner;

    private NetworkCacheDataService networkCacheDataService;

    @Mock
    private QueryGraph queryGraph;
    @Mock
    private TrafficSignSnap trafficSignSnap1;

    @Mock
    private TrafficSignSnap trafficSignSnap2;
    @Mock
    private TrafficSign trafficSign1;

    @Mock
    private TrafficSign trafficSign2;
    @Mock
    private Snap snap;

    @Mock
    private Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey;

    @Mock
    private IsochroneService isochroneService;

    @Mock
    private IsochroneMatch isochroneMatch;
    @Mock
    private Weighting weightingNoRestrictions;

    @Mock
    private RoadSection roadSection;

    @Mock
    private RoadSection clonedRoadSection;


    private TrafficSigns trafficSigns;

    private List<TrafficSignSnap> trafficSignSnapList;

    @BeforeEach
    void setUp() {
        trafficSigns = new TrafficSigns(trafficSign1, trafficSign2);
        trafficSignSnapList = List.of(trafficSignSnap1, trafficSignSnap2);
        networkCacheDataService = new NetworkCacheDataService(queryGraphFactory, trafficSignSnapMapper, isochroneServiceFactory,
                roadSectionMapper, networkGraphHopper, roadSectionTrafficSignAssigner);
    }

    @Test
    void create_shouldInitializeTrafficSignSnapsAndQueryGraph() {

        setupFixtureForCreate();

        assertThatCode(() -> networkCacheDataService.create(trafficSigns))
                .doesNotThrowAnyException();

        assertThat(networkCacheDataService.getQueryGraph()).isEqualTo(queryGraph);
        assertThat(networkCacheDataService.getTrafficSignSnaps(List.of(TRAFFIC_SIGN_ID_1, TRAFFIC_SIGN_ID_2)))
                .isEqualTo(trafficSignSnapList);
        verify(trafficSignSnapMapper).map(trafficSigns.stream().toList());
        verify(queryGraphFactory).createQueryGraph(trafficSignSnapList);
    }

    @Test
    void getTrafficSignSnaps_shouldThrowExceptionIfCreateWasNotCalledBefore() {
        assertThatCode(() -> networkCacheDataService.getTrafficSignSnaps(List.of(TRAFFIC_SIGN_ID_1, TRAFFIC_SIGN_ID_2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No trafficSignSnaps available");
    }

    @Test
    void getQueryGraph_shouldThrowExceptionIfCreateWasNotCalledBefore() {
        assertThatCode(() -> networkCacheDataService.getQueryGraph())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No queryGraph available");
    }

    @Test
    void getBaseAccessibility_shouldComputeForNullMunicipalityIdAndCacheResults() {
        setupFixtureForCreate();
        networkCacheDataService.create(trafficSigns);
        setupFixtureForBaseAccessibleCalculation(null);
        Collection<RoadSection> result = networkCacheDataService.getBaseAccessibility(null, snap, SEARCH_RADIUS_IN_METERS,
                trafficSignsByEdgeKey);
        networkCacheDataService.getBaseAccessibility(null, snap, SEARCH_RADIUS_IN_METERS,
                trafficSignsByEdgeKey);

        verify(roadSectionMapper, times(1))
                .mapToRoadSections(List.of(isochroneMatch));
        verify(networkGraphHopper, times(1)).createWeighting(eq(NetworkConstants.CAR_PROFILE),
                argThat(new PMapArgumentMatcher(new PMap())));
        verify(isochroneServiceFactory, times(1)).createService(networkGraphHopper);
        verify(isochroneService, times(1)).getIsochroneMatchesByMunicipalityId(
                argThat(new IsochroneArgumentMatcher(IsochroneArguments
                        .builder()
                        .weighting(new RestrictionWeightingAdapter(weightingNoRestrictions, Set.of()))
                        .municipalityId(null)
                        .searchDistanceInMetres(SEARCH_RADIUS_IN_METERS)
                        .build())),
                eq(queryGraph), eq(snap));

        assertThat(result).containsExactly(clonedRoadSection);

    }

    @Test
    void getBaseAccessibility_shouldReuseCachedResultsForSpecificMunicipalityId() {
        setupFixtureForCreate();
        networkCacheDataService.create(trafficSigns);
        setupFixtureForBaseAccessibleCalculation(MUNICIPALITY_ID);
        Collection<RoadSection> result = networkCacheDataService.getBaseAccessibility(MUNICIPALITY_ID, snap, SEARCH_RADIUS_IN_METERS,
                trafficSignsByEdgeKey);
        networkCacheDataService.getBaseAccessibility(MUNICIPALITY_ID, snap, SEARCH_RADIUS_IN_METERS,
                trafficSignsByEdgeKey);
        verify(roadSectionMapper, times(1))
                .mapToRoadSections(List.of(isochroneMatch));
        verify(networkGraphHopper, times(1)).createWeighting(eq(NetworkConstants.CAR_PROFILE),
                argThat(new PMapArgumentMatcher(new PMap())));
        verify(isochroneServiceFactory, times(1)).createService(networkGraphHopper);
        verify(isochroneService, times(1)).getIsochroneMatchesByMunicipalityId(
                argThat(new IsochroneArgumentMatcher(IsochroneArguments
                        .builder()
                        .weighting(new RestrictionWeightingAdapter(weightingNoRestrictions, Set.of()))
                        .municipalityId(MUNICIPALITY_ID)
                        .searchDistanceInMetres(SEARCH_RADIUS_IN_METERS)
                        .build())),
                eq(queryGraph), eq(snap));

        assertThat(result).containsExactly(clonedRoadSection);
    }

    @Test
    void getBaseAccessibility_shouldCloneRoadSectionsAndAssignTrafficSigns() {

        setupFixtureForCreate();
        networkCacheDataService.create(trafficSigns);
        setupFixtureForBaseAccessibleCalculation(MUNICIPALITY_ID);

        Collection<RoadSection> result = networkCacheDataService.getBaseAccessibility(MUNICIPALITY_ID, snap, SEARCH_RADIUS_IN_METERS,
                trafficSignsByEdgeKey);

        assertThat(result).containsExactly(clonedRoadSection);
    }

    private void setupFixtureForBaseAccessibleCalculation(Integer municipalityId) {
        when(networkGraphHopper.createWeighting(eq(NetworkConstants.CAR_PROFILE), argThat(new PMapArgumentMatcher(new PMap())))).thenReturn(
                weightingNoRestrictions);
        when(isochroneServiceFactory.createService(networkGraphHopper)).thenReturn(isochroneService);
        when(isochroneService.getIsochroneMatchesByMunicipalityId(
                argThat(new IsochroneArgumentMatcher(IsochroneArguments
                        .builder()
                        .weighting(new RestrictionWeightingAdapter(weightingNoRestrictions, Set.of()))
                        .municipalityId(municipalityId)
                        .searchDistanceInMetres(SEARCH_RADIUS_IN_METERS)
                        .build())),
                eq(queryGraph), eq(snap)))
                .thenReturn(List.of(isochroneMatch));
        when(roadSection.cloneRoadSection()).thenReturn(clonedRoadSection);
        when(roadSectionMapper.mapToRoadSections(List.of(isochroneMatch)))
                .thenReturn(List.of(roadSection));
        when(roadSectionTrafficSignAssigner.assignTrafficSigns(clonedRoadSection, trafficSignsByEdgeKey)).thenReturn(clonedRoadSection);
    }

    private void setupFixtureForCreate() {
        when(trafficSign1.externalId()).thenReturn(TRAFFIC_SIGN_ID_1);
        when(trafficSign2.externalId()).thenReturn(TRAFFIC_SIGN_ID_2);
        when(trafficSignSnap1.getTrafficSign()).thenReturn(trafficSign1);
        when(trafficSignSnap2.getTrafficSign()).thenReturn(trafficSign2);
        when(trafficSignSnapMapper.map(trafficSigns.stream().toList())).thenReturn(trafficSignSnapList);
        when(queryGraphFactory.createQueryGraph(trafficSignSnapList)).thenReturn(queryGraph);
    }

    private record IsochroneArgumentMatcher(IsochroneArguments expected) implements
            ArgumentMatcher<IsochroneArguments> {

        @Override
        public boolean matches(IsochroneArguments actual) {
            // Mockito initializes with null value
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
}
