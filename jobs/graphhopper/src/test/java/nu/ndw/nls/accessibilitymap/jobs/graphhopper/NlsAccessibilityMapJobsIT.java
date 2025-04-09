package nu.ndw.nls.accessibilitymap.jobs.graphhopper;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperNetworkSettingsBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.configuration.GraphHopperProperties;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles(profiles = {"integration-test"})
class NlsAccessibilityMapJobsIT {

    private static final String NETWORK_NAME = "accessibility_latest";

    private static final String PROPERTIES = "properties";

    // Mocking this bean to prevent stderr output about missing PicoCLI commands when running IT
    @MockitoBean
    private AccessibilityMapGraphhoperJobCommandLineRunner accessibilityMapGraphhoperJobCommandLineRunner;

    @Autowired
    private GraphHopperNetworkService networkService;

    @Autowired
    private GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    @Autowired
    private GraphHopperProperties graphHopperProperties;

    @Test
    void createOrUpdateNetwork() throws GraphHopperNotImportedException {
        Path accessibilityLatest = graphHopperNetworkSettingsBuilder.getLatestPath();
        assertTrue(Files.exists(accessibilityLatest));
        // Check whether network is fully built.
        assertTrue(Files.exists(accessibilityLatest.resolve(PROPERTIES)));

        var networkSettings = RoutingNetworkSettings.builder(AccessibilityLink.class)
                .networkNameAndVersion(NETWORK_NAME)
                .profiles(List.of(PROFILE))
                .graphhopperRootPath(graphHopperProperties.getDir())
                .indexed(true)
                .build();

        NetworkGraphHopper networkGraphHopper = networkService.loadFromDisk(networkSettings);
        assertThat(networkGraphHopper).isNotNull();
        assertThat(networkGraphHopper.getImportDate()).isNotNull();
        assertThat(networkGraphHopper.getDataDate()).isNotNull();
        assertTrue(networkGraphHopper.getImportDate().isAfter(networkGraphHopper.getDataDate()));
    }
}
