package nu.ndw.nls.accessibilitymap.jobs;

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
import lombok.SneakyThrows;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.IndexedGraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.graphhopper.IndexedNetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.graphhopper.ev.EncodedTag;
import nu.ndw.nls.routingmapmatcher.graphhopper.ev.EncodedTag.EncodingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles(profiles = {"integration-test"})
class NlsAccessibilityMapJobsIT {

    private static final String ACCESSIBILITY_LATEST = "accessibility_latest";
    private static final String PROPERTIES = "properties";
    private static final Instant EXPECTED_DATA_DATE = Instant.parse("2023-11-10T13:51:59Z");

    // Mocking this bean to prevent stderr output about missing PicoCLI commands when running IT
    @MockBean
    private JobsCommandLineRunner jobsCommandLineRunner;

    @Autowired
    private IndexedGraphHopperNetworkService indexedGraphHopperNetworkService;

    @Value("${graphhopper.dir}")
    private String graphHopperDir;

    @SneakyThrows
    @Test
    void createOrUpdateNetwork_ok() {
        Path graphHopperPath = Path.of(graphHopperDir);
        Path accessibilityLatest = graphHopperPath.resolve(ACCESSIBILITY_LATEST);
        assertTrue(Files.exists(accessibilityLatest));
        // Check whether network is fully built.
        assertTrue(Files.exists(accessibilityLatest.resolve(PROPERTIES)));

        IndexedNetworkGraphHopper networkGraphHopper = indexedGraphHopperNetworkService.loadFromDisk(
                RoutingNetwork.builder().networkNameAndVersion(ACCESSIBILITY_LATEST).build(), graphHopperPath);
        assertThat(networkGraphHopper).isNotNull();
        assertThat(networkGraphHopper.getImportDate()).isNotNull();
        assertThat(networkGraphHopper.getDataDate()).isEqualTo(EXPECTED_DATA_DATE);

        // Text sign type TIJD
        assertEdgeValue(networkGraphHopper, 319325003, EncodedTag.MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, false);
        // Text sign type UIT
        assertEdgeValue(networkGraphHopper, 600793741, EncodedTag.MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, false);
        // Text sign type VOOR
        assertEdgeValue(networkGraphHopper, 309327059, EncodedTag.MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, false);

        // Driving direction H
        assertEdgeValue(networkGraphHopper, 310325117, EncodedTag.MOTOR_VEHICLE_ACCESS_FORBIDDEN, true, false);
        // Driving direction T
        assertEdgeValue(networkGraphHopper, 310326129, EncodedTag.MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, true);

        // Black code - driving direction H
        assertEdgeValue(networkGraphHopper, 316335071, EncodedTag.MAX_HEIGHT, 4.0, Double.POSITIVE_INFINITY);
        // Black code - driving direction null
        assertEdgeValue(networkGraphHopper, 600389685, EncodedTag.MAX_LENGTH, 10.0, 10.0);
    }

    private void assertEdgeValue(IndexedNetworkGraphHopper networkGraphHopper, long roadSectionId,
            EncodedTag encodedTag, Object expectedForwardValue, Object expectedBackwardValue) {
        Integer edgeKey = networkGraphHopper.getEdgeMap().get(roadSectionId);
        assertNotNull(edgeKey);
        EdgeIteratorState edge = networkGraphHopper.getBaseGraph().getEdgeIteratorStateForKey(edgeKey);
        if (encodedTag.getEncodingType() == EncodingType.BOOLEAN) {
            BooleanEncodedValue encodedValue = networkGraphHopper.getEncodingManager()
                    .getBooleanEncodedValue(encodedTag.getKey());
            assertEquals(expectedForwardValue, edge.get(encodedValue));
            assertEquals(expectedBackwardValue, edge.getReverse(encodedValue));
        } else if (encodedTag.getEncodingType() == EncodingType.DECIMAL) {
            DecimalEncodedValue encodedValue = networkGraphHopper.getEncodingManager()
                    .getDecimalEncodedValue(encodedTag.getKey());
            assertEquals(expectedForwardValue, edge.get(encodedValue));
            assertEquals(expectedBackwardValue, edge.getReverse(encodedValue));
        }
    }
}
