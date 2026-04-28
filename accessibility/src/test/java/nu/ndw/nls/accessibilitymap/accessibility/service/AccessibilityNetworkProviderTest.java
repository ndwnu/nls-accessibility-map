package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.Snap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.SnapRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.Snapper;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.WeightingFactory;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.service.exception.AccessibilityLocationNotFoundException;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNetworkProviderTest {

    private AccessibilityNetworkProvider accessibilityNetworkProvider;

    @Mock
    private QueryGraphConfigurer queryGraphConfigurer;

    @Mock
    private Snapper snapper;

    @Mock
    private NetworkData networkData;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private BaseGraph baseGraph;

    @Mock
    private Restriction restriction;

    @Mock
    private Snap fromSnap;

    @Mock
    private Snap destinationSnap;

    @Mock
    private Snap restrictionSnap;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private WeightingFactory weightingFactory;


    @Mock
    private Weighting weightingWithRestrictions;

    @Mock
    private Weighting weightingWithoutRestrictions;

    private Location from;

    private Location destination;

    @BeforeEach
    void setUp() {

        accessibilityNetworkProvider = new AccessibilityNetworkProvider(queryGraphConfigurer, snapper, weightingFactory);

        from = new Location(52.0, 4.0, null);
        destination = new Location(52.1, 4.1, null);
    }

    @Test
    void get() {

        Restrictions restrictions = new Restrictions(List.of(restriction));
        Map<Integer, List<Restriction>> restrictionsById = Map.of(1, List.of(restriction));

        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(snapper.snapLocation(networkGraphHopper, from)).thenReturn(Optional.of(fromSnap));
        when(snapper.snapLocation(networkGraphHopper, destination)).thenReturn(Optional.of(destinationSnap));

        when(snapper.snapRestriction(networkGraphHopper, restriction)).thenReturn(Optional.of(restrictionSnap));

        when(queryGraphConfigurer.createEdgeRestrictions(
                eq(encodingManager),
                eq(queryGraph),
                assertArg(snapRestrictions -> {
                    assertThat(snapRestrictions).hasSize(1);
                    SnapRestriction snapRestriction = snapRestrictions.getFirst();
                    assertThat(snapRestriction.snap()).isEqualTo(restrictionSnap);
                    assertThat(snapRestriction.restriction()).isEqualTo(restriction);
                }))).thenReturn(restrictionsById);

        try (var queryGraphMockStatic = Mockito.mockStatic(QueryGraph.class)) {
            queryGraphMockStatic.when(() -> QueryGraph.create(baseGraph, List.of(restrictionSnap, fromSnap, destinationSnap)))
                    .thenReturn(queryGraph);
            when(weightingFactory.createWeighting(networkData, queryGraph, restrictionsById.keySet()))
                    .thenReturn(weightingWithRestrictions);
            when(weightingFactory.createWeighting(networkData, queryGraph, Set.of()))
                    .thenReturn(weightingWithoutRestrictions);

            AccessibilityNetwork accessibilityNetwork = accessibilityNetworkProvider.get(
                    networkData,
                    restrictions,
                    from,
                    destination);

            assertThat(accessibilityNetwork.getNetworkData()).isEqualTo(networkData);
            assertThat(accessibilityNetwork.getQueryGraph()).isEqualTo(queryGraph);
            assertThat(accessibilityNetwork.getRestrictions()).isEqualTo(restrictions);
            assertThat(accessibilityNetwork.getRestrictionsByEdgeKey()).isEqualTo(restrictionsById);
            assertThat(accessibilityNetwork.getFrom()).isEqualTo(fromSnap);
            assertThat(accessibilityNetwork.getDestination()).isEqualTo(destinationSnap);
            assertThat(accessibilityNetwork.getWeightingWithRestrictions()).isEqualTo(weightingWithRestrictions);
            assertThat(accessibilityNetwork.getWeightingWithOutRestrictions()).isEqualTo(weightingWithoutRestrictions);
        }
    }

    @Test
    void get_fromLocationCouldNotBeSnapped() {
        Restrictions restrictions = new Restrictions(List.of(restriction));

        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(snapper.snapLocation(networkGraphHopper, from)).thenReturn(Optional.of(fromSnap));
        when(snapper.snapLocation(networkGraphHopper, destination)).thenReturn(Optional.empty());

        assertThat(catchThrowable(() -> accessibilityNetworkProvider.get(
                networkData,
                restrictions,
                from,
                destination)))
                .isInstanceOf(AccessibilityLocationNotFoundException.class)
                .hasMessage(("Location could not be resolved at %s, %s. Please try a different location that is closer to actual "
                             + "road sections in the network.").formatted(from.latitude(), from.longitude()));
    }

    @Test
    void get_destinationLocationCouldNotBeSnapped() {
        Restrictions restrictions = new Restrictions(List.of(restriction));

        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(snapper.snapLocation(networkGraphHopper, from)).thenReturn(Optional.empty());

        assertThat(catchThrowable(() -> accessibilityNetworkProvider.get(
                networkData,
                restrictions,
                from,
                destination)))
                .isInstanceOf(AccessibilityLocationNotFoundException.class)
                .hasMessage(("Location could not be resolved at %s, %s. Please try a different location that is closer to actual "
                             + "road sections in the network.").formatted(from.latitude(), from.longitude()));
    }

    @Test
    void get_noDestination() {

        Restrictions restrictions = new Restrictions(List.of(restriction));
        Map<Integer, List<Restriction>> restrictionsById = Map.of(1, List.of(restriction));

        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(snapper.snapLocation(networkGraphHopper, from)).thenReturn(Optional.of(fromSnap));
        when(snapper.snapLocation(networkGraphHopper, null)).thenReturn(Optional.of(destinationSnap));
        when(snapper.snapRestriction(networkGraphHopper, restriction)).thenReturn(Optional.of(restrictionSnap));
        when(queryGraphConfigurer.createEdgeRestrictions(
                eq(encodingManager),
                eq(queryGraph),
                assertArg(snapRestrictions -> {
                    assertThat(snapRestrictions).hasSize(1);
                    SnapRestriction snapRestriction = snapRestrictions.getFirst();
                    assertThat(snapRestriction.snap()).isEqualTo(restrictionSnap);
                    assertThat(snapRestriction.restriction()).isEqualTo(restriction);
                }))).thenReturn(Map.of(1, List.of(restriction)));

        try (var queryGraphMockStatic = Mockito.mockStatic(QueryGraph.class)) {
            queryGraphMockStatic.when(() -> QueryGraph.create(baseGraph, List.of(restrictionSnap, fromSnap, destinationSnap)))
                    .thenReturn(queryGraph);
            when(weightingFactory.createWeighting(networkData, queryGraph, restrictionsById.keySet()))
                    .thenReturn(weightingWithRestrictions);
            when(weightingFactory.createWeighting(networkData, queryGraph, Set.of()))
                    .thenReturn(weightingWithoutRestrictions);

            AccessibilityNetwork accessibilityNetwork = accessibilityNetworkProvider.get(
                    networkData,
                    restrictions,
                    from,
                    null);

            assertThat(accessibilityNetwork.getNetworkData()).isEqualTo(networkData);
            assertThat(accessibilityNetwork.getQueryGraph()).isEqualTo(queryGraph);
            assertThat(accessibilityNetwork.getRestrictions()).isEqualTo(restrictions);
            assertThat(accessibilityNetwork.getRestrictionsByEdgeKey()).isEqualTo(Map.of(1, List.of(restriction)));
            assertThat(accessibilityNetwork.getFrom()).isEqualTo(fromSnap);
            assertThat(accessibilityNetwork.getDestination()).isEqualTo(destinationSnap);
            assertThat(accessibilityNetwork.getWeightingWithRestrictions()).isEqualTo(weightingWithRestrictions);
            assertThat(accessibilityNetwork.getWeightingWithOutRestrictions()).isEqualTo(weightingWithoutRestrictions);
        }
    }

    @Test
    void get_restrictionCouldNotBeSnapped() {

        Restrictions restrictions = new Restrictions(List.of(restriction));

        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(snapper.snapLocation(networkGraphHopper, from)).thenReturn(Optional.of(fromSnap));
        when(snapper.snapLocation(networkGraphHopper, destination)).thenReturn(Optional.of(destinationSnap));
        when(snapper.snapRestriction(networkGraphHopper, restriction)).thenReturn(Optional.empty());
        when(queryGraphConfigurer.createEdgeRestrictions(
                eq(encodingManager),
                eq(queryGraph),
                assertArg(snapRestrictions -> assertThat(snapRestrictions).isEmpty()))
        ).thenReturn(Map.of(1, List.of(restriction)));

        try (var queryGraphMockStatic = Mockito.mockStatic(QueryGraph.class)) {
            queryGraphMockStatic.when(() -> QueryGraph.create(baseGraph, List.of(fromSnap, destinationSnap)))
                    .thenReturn(queryGraph);

            AccessibilityNetwork accessibilityNetwork = accessibilityNetworkProvider.get(
                    networkData,
                    restrictions,
                    from,
                    destination);

            assertThat(accessibilityNetwork.getNetworkData()).isEqualTo(networkData);
            assertThat(accessibilityNetwork.getQueryGraph()).isEqualTo(queryGraph);
            assertThat(accessibilityNetwork.getRestrictions()).isEqualTo(restrictions);
            assertThat(accessibilityNetwork.getRestrictionsByEdgeKey()).isEqualTo(Map.of(1, List.of(restriction)));
            assertThat(accessibilityNetwork.getFrom()).isEqualTo(fromSnap);
            assertThat(accessibilityNetwork.getDestination()).isEqualTo(destinationSnap);
        }
    }
}
