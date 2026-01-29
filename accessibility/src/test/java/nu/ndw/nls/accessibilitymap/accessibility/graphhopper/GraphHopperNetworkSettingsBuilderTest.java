package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.configuration.GraphHopperProperties;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperNetworkSettingsBuilderTest {

    private static final String NETWORK_NAME = "accessibility_latest";

    private static final Path GRAPHHOPPER_BASE_PATH = Path.of("base_path");

    private static final Path GRAPHHOPPER_FULL_PATH = Path.of("base_path", NETWORK_NAME);

    private static final Path EXPECTED_METADATA_PATH = Path.of("base_path/accessibility_latest/accessibility_metadata.json");

    private GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    @Mock
    private GraphHopperProperties graphHopperProperties;

    @Mock
    private List<AccessibilityLink> accessibilityLinks;

    @Mock
    private Iterator<AccessibilityLink> accessibilityLinksIterator;

    @Mock
    private Instant trafficSignData;

    @BeforeEach
    void setUp() {

        graphHopperNetworkSettingsBuilder = new GraphHopperNetworkSettingsBuilder(graphHopperProperties);
    }

    @Test
    void publishEvents() {

        when(graphHopperProperties.isPublishEvents()).thenReturn(true);
        assertThat(graphHopperNetworkSettingsBuilder.publishEvents()).isTrue();
    }

    @Test
    void getLatestPath() {

        when(graphHopperProperties.getLatestPath()).thenReturn(GRAPHHOPPER_FULL_PATH);
        assertThat(graphHopperNetworkSettingsBuilder.getLatestPath()).isEqualTo(GRAPHHOPPER_FULL_PATH);
    }

    @Test
    void getMetadataPath() {

        when(graphHopperProperties.getMetadataPath()).thenReturn(EXPECTED_METADATA_PATH);
        assertThat(graphHopperNetworkSettingsBuilder.getMetadataPath()).isEqualTo(EXPECTED_METADATA_PATH);
    }

    @Test
    void defaultNetworkSettings() {

        when(graphHopperProperties.getDir()).thenReturn(GRAPHHOPPER_BASE_PATH);
        when(graphHopperProperties.getNetworkName()).thenReturn(NETWORK_NAME);

        RoutingNetworkSettings<AccessibilityLink> accessibilityLinkRoutingNetworkSettings = graphHopperNetworkSettingsBuilder.defaultNetworkSettings();

        assertEquals(
                RoutingNetworkSettings
                        .builder(AccessibilityLink.class)
                        .indexed(true)
                        .graphhopperRootPath(GRAPHHOPPER_BASE_PATH)
                        .networkNameAndVersion(NETWORK_NAME)
                        .profiles(List.of(CAR_PROFILE))
                        .build(), accessibilityLinkRoutingNetworkSettings);
    }

    @Test
    void networkSettingsWithData() {

        when(graphHopperProperties.getDir()).thenReturn(GRAPHHOPPER_BASE_PATH);
        when(graphHopperProperties.getNetworkName()).thenReturn(NETWORK_NAME);
        when(accessibilityLinks.iterator()).thenReturn(accessibilityLinksIterator);

        RoutingNetworkSettings<AccessibilityLink> accessibilityLinkRoutingNetworkSettings = graphHopperNetworkSettingsBuilder.networkSettingsWithData(
                accessibilityLinks,
                trafficSignData);

        assertThat(accessibilityLinkRoutingNetworkSettings.getGraphhopperRootPath()).isEqualTo(GRAPHHOPPER_BASE_PATH);
        assertThat(accessibilityLinkRoutingNetworkSettings.getNetworkNameAndVersion()).isEqualTo(NETWORK_NAME);
        assertThat(accessibilityLinkRoutingNetworkSettings.getProfiles()).containsExactly(CAR_PROFILE);
        assertThat(accessibilityLinkRoutingNetworkSettings.isIndexed()).isTrue();
        assertThat(accessibilityLinkRoutingNetworkSettings.getLinkSupplier().get()).isEqualTo(accessibilityLinksIterator);
        assertThat(accessibilityLinkRoutingNetworkSettings.getDataDate()).isEqualTo(trafficSignData);
    }
}
