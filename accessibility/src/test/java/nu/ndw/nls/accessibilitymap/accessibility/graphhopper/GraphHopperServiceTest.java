package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
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
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory("testDir");
        graphHopperService = new GraphHopperService(
                graphHopperNetworkSettingsBuilder,
                graphHopperNetworkService,
                networkMetaDataService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void getNetworkGraphHopper() throws GraphHopperNotImportedException {

        AtomicBoolean updateCalled = new AtomicBoolean(false);
        graphHopperService.registerUpdateListener(() -> updateCalled.set(true));

        mockLoadingGraphHopper();
        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);

        GraphHopperNetwork graphHopperNetwork1 = graphHopperService.getNetworkGraphHopper();

        assertThat(graphHopperNetwork1.network()).isEqualTo(networkGraphHopper);
        assertThat(graphHopperNetwork1.nwbVersion()).isEqualTo(123);

        GraphHopperNetwork graphHopperNetwork2 = graphHopperService.getNetworkGraphHopper();
        assertThat(graphHopperNetwork2).isSameAs(graphHopperNetwork1);

        verify(graphHopperNetworkService).loadFromDisk(routingNetworkSettings);
        assertThat(updateCalled.get()).isTrue();
    }

    private void mockLoadingGraphHopper() throws GraphHopperNotImportedException {
        when(graphHopperNetworkSettingsBuilder.defaultNetworkSettings()).thenReturn(routingNetworkSettings);
        when(routingNetworkSettings.getNetworkNameAndVersion()).thenReturn("version");
        when(routingNetworkSettings.getGraphhopperRootPath()).thenReturn(testDir.resolve(Path.of("graphhopper")));
        when(graphHopperNetworkService.loadFromDisk(routingNetworkSettings)).thenReturn(networkGraphHopper);
    }

    @Test
    void loadNewGraphHopperNetwork() throws GraphHopperNotImportedException {

        AtomicBoolean updateCalled = new AtomicBoolean(false);
        graphHopperService.registerUpdateListener(() -> updateCalled.set(true));

        mockLoadingGraphHopper();
        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);

        graphHopperService.loadNewGraphHopperNetwork();

        GraphHopperNetwork graphHopperNetwork = graphHopperService.getNetworkGraphHopper();
        assertThat(graphHopperNetwork.network()).isEqualTo(networkGraphHopper);
        assertThat(graphHopperNetwork.nwbVersion()).isEqualTo(123);

        verify(graphHopperNetworkService).loadFromDisk(routingNetworkSettings);
        assertThat(updateCalled.get()).isTrue();
    }

    @Test
    void loadNewGraphHopper_Network_error() throws GraphHopperNotImportedException {

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

        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);

        graphHopperService.loadNewGraphHopperNetwork();

        assertThat(updateCalled.get()).isTrue();
    }
}
