package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.Snap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.SnapRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.Snapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityException;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperServiceTest {

    private GraphHopperService graphHopperService;

    @Mock
    private GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    @Mock
    private GraphHopperNetworkService graphHopperNetworkService;

    @Mock
    private RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings;

    @Mock
    private QueryGraphConfigurer queryGraphConfigurer;

    @Mock
    private Snapper snapper;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @Mock
    private Restriction restriction;

    @Mock
    private Location from;

    @Mock
    private Snap fromSnap;

    @Mock
    private Location destination;

    @Mock
    private Snap destinationSnap;

    @Mock
    private Snap restrictionSnap;

    @Mock
    private BaseGraph baseGraph;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;
    @Mock
    private SnapRestriction snapRestriction;

    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory("testDir");
        graphHopperService = new GraphHopperService(
                graphHopperNetworkSettingsBuilder,
                graphHopperNetworkService,
                queryGraphConfigurer,
                snapper,
                networkMetaDataService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void getNetwork() throws GraphHopperNotImportedException {
        Restrictions restrictions = new Restrictions();
        restrictions.add(restriction);

        mockLoadingGraphHopper();
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);

        when(snapper.snapLocation(networkGraphHopper, from)).thenReturn(Optional.of(fromSnap));
        when(snapper.snapLocation(networkGraphHopper, destination)).thenReturn(Optional.of(destinationSnap));
        when(snapper.snapRestriction(networkGraphHopper, restriction)).thenReturn(Optional.of(restrictionSnap));

        try (MockedStatic<QueryGraph> queryGraphStaticMock = Mockito.mockStatic(QueryGraph.class)) {
            queryGraphStaticMock.when(() -> QueryGraph.create(baseGraph, List.of(restrictionSnap, fromSnap, destinationSnap)))
                    .thenReturn(queryGraph);
            when(queryGraphConfigurer.createEdgeRestrictions(queryGraph, List.of(new SnapRestriction(restrictionSnap, restriction))))
                    .thenReturn(Map.of(2, List.of(restriction)));

            GraphHopperNetwork graphHopperNetwork = graphHopperService.getNetwork(restrictions, from, destination);

            assertThat(graphHopperNetwork.getQueryGraph()).isEqualTo(queryGraph);
            assertThat(graphHopperNetwork.getNetwork()).isEqualTo(networkGraphHopper);
            assertThat(graphHopperNetwork.getNwbVersion()).isEqualTo(123);
            assertThat(graphHopperNetwork.getFrom()).isEqualTo(fromSnap);
            assertThat(graphHopperNetwork.getDestination()).isEqualTo(destinationSnap);
            assertThat(graphHopperNetwork.getRestrictions()).isEqualTo(restrictions);
            assertThat(graphHopperNetwork.getBlockedEdges()).isEqualTo(Set.of(2));
            assertThat(graphHopperNetwork.getRestrictionsByEdgeKey()).isEqualTo(Map.of(2, List.of(restriction)));
        }
    }

    @Test
    void getNetwork_noFromSnap() throws GraphHopperNotImportedException {
        Restrictions restrictions = new Restrictions();
        restrictions.add(restriction);

        mockLoadingGraphHopper();
        when(from.latitude()).thenReturn(1.0);
        when(from.longitude()).thenReturn(2.0);
        when(snapper.snapLocation(networkGraphHopper, from)).thenReturn(Optional.empty());

        assertThat(catchThrowable(() -> graphHopperService.getNetwork(restrictions, from, destination)))
                .isInstanceOf(AccessibilityException.class)
                .hasMessage("Could not find a snap point for from location (1.0, 2.0).");
    }

    @Test
    void getNetwork_noDestinationSnap() throws GraphHopperNotImportedException {
        Restrictions restrictions = new Restrictions();
        restrictions.add(restriction);

        mockLoadingGraphHopper();
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);

        when(snapper.snapLocation(networkGraphHopper, from)).thenReturn(Optional.of(fromSnap));
        when(snapper.snapLocation(networkGraphHopper, destination)).thenReturn(Optional.empty());
        when(snapper.snapRestriction(networkGraphHopper, restriction)).thenReturn(Optional.of(restrictionSnap));

        try (MockedStatic<QueryGraph> queryGraphStaticMock = Mockito.mockStatic(QueryGraph.class)) {
            queryGraphStaticMock.when(() -> QueryGraph.create(baseGraph, List.of(restrictionSnap, fromSnap)))
                    .thenReturn(queryGraph);
            when(queryGraphConfigurer.createEdgeRestrictions(queryGraph, List.of(new SnapRestriction(restrictionSnap, restriction))))
                    .thenReturn(Map.of(2, List.of(restriction)));

            GraphHopperNetwork graphHopperNetwork = graphHopperService.getNetwork(restrictions, from, destination);

            assertThat(graphHopperNetwork.getQueryGraph()).isEqualTo(queryGraph);
            assertThat(graphHopperNetwork.getNetwork()).isEqualTo(networkGraphHopper);
            assertThat(graphHopperNetwork.getNwbVersion()).isEqualTo(123);
            assertThat(graphHopperNetwork.getFrom()).isEqualTo(fromSnap);
            assertThat(graphHopperNetwork.getDestination()).isNull();
            assertThat(graphHopperNetwork.getRestrictions()).isEqualTo(restrictions);
            assertThat(graphHopperNetwork.getBlockedEdges()).isEqualTo(Set.of(2));
            assertThat(graphHopperNetwork.getRestrictionsByEdgeKey()).isEqualTo(Map.of(2, List.of(restriction)));
        }
    }

    private void mockLoadingGraphHopper() throws GraphHopperNotImportedException {
        when(graphHopperNetworkSettingsBuilder.defaultNetworkSettings()).thenReturn(routingNetworkSettings);
        when(routingNetworkSettings.getNetworkNameAndVersion()).thenReturn("version");
        when(routingNetworkSettings.getGraphhopperRootPath()).thenReturn(testDir.resolve(Path.of("graphhopper")));
        when(graphHopperNetworkService.loadFromDisk(routingNetworkSettings)).thenReturn(networkGraphHopper);
    }

    @Test
    void loadNewNetworkGraphHopper() throws GraphHopperNotImportedException {

        AtomicBoolean updateCalled = new AtomicBoolean(false);
        graphHopperService.registerUpdateListener(() -> updateCalled.set(true));

        mockLoadingGraphHopper();

        assertThat(graphHopperService.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(Files.isDirectory(testDir.resolve(Path.of("graphhopper")).resolve(Path.of("version")))).isTrue();

        assertThat(graphHopperService.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        verify(graphHopperNetworkService).loadFromDisk(routingNetworkSettings);
        assertThat(updateCalled.get()).isTrue();
    }

    @Test
    void loadNewNetworkGraphHopper_error() throws GraphHopperNotImportedException {

        GraphHopperNotImportedException cause = new GraphHopperNotImportedException("some error");

        when(graphHopperNetworkSettingsBuilder.defaultNetworkSettings()).thenReturn(routingNetworkSettings);
        when(routingNetworkSettings.getNetworkNameAndVersion()).thenReturn("version");
        when(routingNetworkSettings.getGraphhopperRootPath()).thenReturn(testDir.resolve(Path.of("graphhopper")));
        when(graphHopperNetworkService.loadFromDisk(routingNetworkSettings)).thenThrow(cause);

        assertThat(catchThrowable(() -> graphHopperService.getNetworkGraphHopper()))
                .hasMessage("Could not load network GraphHopper from %s".formatted(testDir.resolve(Path.of("graphhopper"))))
                .hasCause(cause)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void registerUpdateListener() throws GraphHopperNotImportedException {

        mockLoadingGraphHopper();

        AtomicBoolean updateCalled = new AtomicBoolean(false);
        graphHopperService.registerUpdateListener(() -> updateCalled.set(true));

        graphHopperService.loadNewNetworkGraphHopper();

        assertThat(updateCalled.get()).isTrue();
    }
}
