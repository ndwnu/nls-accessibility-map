package nu.ndw.nls.accessibilitymap.jobs.graphhopper;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_HEIGHT;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_WIDTH;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles(profiles = {"integration-test"})
class NlsAccessibilityMapJobsIT {

    private static final String NETWORK_NAME = "accessibility_latest";
    private static final String PROPERTIES = "properties";

    // Mocking this bean to prevent stderr output about missing PicoCLI commands when running IT
    @MockBean
    private AccessibilityMapGraphhoperJobCommandLineRunner accessibilityMapGraphhoperJobCommandLineRunner;

    @Autowired
    private GraphHopperNetworkService networkService;

    @Autowired
    private GraphHopperConfiguration graphHopperConfiguration;

    @Autowired
    private GraphHopperProperties graphHopperProperties;

    @SneakyThrows
    @Test
    void createOrUpdateNetwork_ok() {
        Path accessibilityLatest = graphHopperConfiguration.getLatestPath();
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

        // Text sign type TIJD with windowed restriction
        assertEdgeValue(networkGraphHopper, 307324006, HGV_ACCESS_FORBIDDEN_WINDOWED, false, false);
        // Text sign type TIJD
        assertEdgeValue(networkGraphHopper, 319325003, MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, false);
        // Text sign type UIT
        assertEdgeValue(networkGraphHopper, 600793741, MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, false);
        // Text sign type VOOR
        assertEdgeValue(networkGraphHopper, 309327059, MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, false);
        // Text sign type VRIJ
        assertEdgeValue(networkGraphHopper, 601008374, MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, false);

        // Driving direction H
        assertEdgeValue(networkGraphHopper, 310325117, MOTOR_VEHICLE_ACCESS_FORBIDDEN, true, false);
        // Driving direction T
        assertEdgeValue(networkGraphHopper, 310326129, MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, true);

        // Black code - driving direction H
        assertEdgeValue(networkGraphHopper, 316335071, MAX_HEIGHT, 4.0, Double.POSITIVE_INFINITY);
        // Black code - driving direction null
        assertEdgeValue(networkGraphHopper, 600137823, MAX_WIDTH, 3.5, 3.5);
    }

    private void assertEdgeValue(NetworkGraphHopper networkGraphHopper, long roadSectionId,
            String key, boolean expectedForwardValue, boolean expectedBackwardValue) {
        Integer edgeKey = networkGraphHopper.getEdgeMap().get(roadSectionId);
        assertNotNull(edgeKey);
        EdgeIteratorState edge = networkGraphHopper.getBaseGraph().getEdgeIteratorStateForKey(edgeKey);
        BooleanEncodedValue encodedValue = networkGraphHopper.getEncodingManager()
                .getBooleanEncodedValue(key);
        assertEquals(expectedForwardValue, edge.get(encodedValue));
        assertEquals(expectedBackwardValue, edge.getReverse(encodedValue));
    }

    private void assertEdgeValue(NetworkGraphHopper networkGraphHopper, long roadSectionId,
            String key, double expectedForwardValue, double expectedBackwardValue) {
        Integer edgeKey = networkGraphHopper.getEdgeMap().get(roadSectionId);
        assertNotNull(edgeKey);
        EdgeIteratorState edge = networkGraphHopper.getBaseGraph().getEdgeIteratorStateForKey(edgeKey);
        DecimalEncodedValue encodedValue = networkGraphHopper.getEncodingManager()
                .getDecimalEncodedValue(key);
        assertEquals(expectedForwardValue, edge.get(encodedValue));
        assertEquals(expectedBackwardValue, edge.getReverse(encodedValue));
    }
}
