package nu.ndw.nls.accessibilitymap.shared.properties;

import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperConfigurationTest {

    private static final String NETWORK_NAME = "accessibility_latest";
    private static final Path GRAPHHOPPER_BASE_PATH = Path.of("base_path");
    private static final Path GRAPHHOPPER_FULL_PATH = Path.of("base_path", NETWORK_NAME);
    private static final Path EXPECTED_META_DATA_PATH = Path.of(
            "base_path/accessibility_latest/accessibility_meta_data.json");

    @Mock
    private GraphHopperProperties graphHopperProperties;

    @InjectMocks
    private GraphHopperConfiguration graphHopperConfiguration;

    @Mock
    private Supplier<Iterator<AccessibilityLink>> accessibilityLinkSupplier;

    @Mock
    private Instant trafficSignData;

    @Test
    void getLatestPath_ok() {
        when(graphHopperProperties.getLatestPath()).thenReturn(GRAPHHOPPER_FULL_PATH);
        assertEquals(GRAPHHOPPER_FULL_PATH, graphHopperConfiguration.getLatestPath());
    }

    @Test
    void getMetaDataPath_ok() {
        when(graphHopperProperties.getMetaDataPath()).thenReturn(EXPECTED_META_DATA_PATH);
        assertEquals(EXPECTED_META_DATA_PATH, graphHopperConfiguration.getMetaDataPath());
    }


    @Test
    void configureLoadingRoutingNetworkSettings_ok() {
        when(graphHopperProperties.getDir()).thenReturn(GRAPHHOPPER_BASE_PATH);
        when(graphHopperProperties.getNetworkName()).thenReturn(NETWORK_NAME);

        RoutingNetworkSettings<AccessibilityLink> accessibilityLinkRoutingNetworkSettings =
                graphHopperConfiguration.configureLoadingRoutingNetworkSettings();

        assertEquals(RoutingNetworkSettings
                .builder(AccessibilityLink.class)
                .indexed(true)
                .graphhopperRootPath(GRAPHHOPPER_BASE_PATH)
                .networkNameAndVersion(NETWORK_NAME)
                .profiles(List.of(PROFILE))
                .build(), accessibilityLinkRoutingNetworkSettings);
    }

    @Test
    void configurePersistingRoutingNetworkSettings_ok() {
        when(graphHopperProperties.getDir()).thenReturn(GRAPHHOPPER_BASE_PATH);
        when(graphHopperProperties.getNetworkName()).thenReturn(NETWORK_NAME);
        RoutingNetworkSettings<AccessibilityLink> accessibilityLinkRoutingNetworkSettings =
                graphHopperConfiguration.configurePersistingRoutingNetworkSettings(accessibilityLinkSupplier,
                        trafficSignData);

        assertEquals(RoutingNetworkSettings
                .builder(AccessibilityLink.class)
                .graphhopperRootPath(GRAPHHOPPER_BASE_PATH)
                .networkNameAndVersion(NETWORK_NAME)
                .profiles(List.of(PROFILE))
                .indexed(true)
                .linkSupplier(accessibilityLinkSupplier)
                .dataDate(trafficSignData)
                .build(), accessibilityLinkRoutingNetworkSettings);
    }
}
