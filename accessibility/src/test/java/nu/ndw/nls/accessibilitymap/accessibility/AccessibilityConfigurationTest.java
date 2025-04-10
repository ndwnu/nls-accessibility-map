package nu.ndw.nls.accessibilitymap.accessibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperNetworkSettingsBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
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
class AccessibilityConfigurationTest {

    private GraphhopperConfiguration accessibilityConfiguration;

    @Mock
    private GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    @Mock
    private GraphHopperNetworkService graphHopperNetworkService;

    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @Mock
    private RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        testDir = Files.createTempDirectory("testDir");

        accessibilityConfiguration = new GraphhopperConfiguration(graphHopperNetworkSettingsBuilder, graphHopperNetworkService,
                networkMetaDataService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    @SneakyThrows
    void networkGraphHopper() {

        when(graphHopperNetworkSettingsBuilder.defaultNetworkSettings()).thenReturn(routingNetworkSettings);
        when(routingNetworkSettings.getNetworkNameAndVersion()).thenReturn("version");
        when(routingNetworkSettings.getGraphhopperRootPath()).thenReturn(testDir.resolve(Path.of("graphhopper")));
        when(graphHopperNetworkService.loadFromDisk(routingNetworkSettings)).thenReturn(networkGraphHopper);

        assertThat(accessibilityConfiguration.networkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(Files.isDirectory(testDir.resolve(Path.of("graphhopper")).resolve(Path.of("version")))).isTrue();
    }

    @Test
    @SneakyThrows
    void getMetaData() {

        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);

        assertEquals(graphhopperMetaData, accessibilityConfiguration.getMetaData());
    }

    @Test
    void edgeIteratorStateReverseExtractor() {

        assertNotNull(accessibilityConfiguration.edgeIteratorStateReverseExtractor());

    }
}