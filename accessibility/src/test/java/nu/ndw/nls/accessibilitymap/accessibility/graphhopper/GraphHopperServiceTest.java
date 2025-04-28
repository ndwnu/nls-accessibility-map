package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    private NetworkGraphHopper networkGraphHopper;

    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory("testDir");
        graphHopperService = new GraphHopperService(graphHopperNetworkSettingsBuilder, graphHopperNetworkService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

//    @Test
//    @SneakyThrows
//    void getNetworkGraphHopper() {
//
//        when(graphHopperNetworkSettingsBuilder.defaultNetworkSettings()).thenReturn(routingNetworkSettings);
//        when(routingNetworkSettings.getNetworkNameAndVersion()).thenReturn("version");
//        when(routingNetworkSettings.getGraphhopperRootPath()).thenReturn(testDir.resolve(Path.of("graphhopper")));
//        when(graphHopperNetworkService.loadFromDisk(routingNetworkSettings)).thenReturn(networkGraphHopper);
//
//        assertThat(graphHopperService.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
//        assertThat(Files.isDirectory(testDir.resolve(Path.of("graphhopper")).resolve(Path.of("version")))).isTrue();
//
//        assertThat(graphHopperService.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
//        verify(graphHopperNetworkService).loadFromDisk(routingNetworkSettings);
//    }
}