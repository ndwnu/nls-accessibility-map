package nu.ndw.nls.accessibilitymap.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import lombok.SneakyThrows;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.AccessibilityGraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.graphhopper.NetworkGraphHopper;
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
public class NlsAccessibilityMapJobsIT {

    private static final String ACCESSIBILITY_LATEST = "accessibility_latest";
    private static final String PROPERTIES = "properties";
    private static final Instant EXPECTED_DATA_DATE = Instant.parse("2023-11-02T18:13:00Z");

    // Mocking this bean to prevent stderr output about missing PicoCLI commands when running IT
    @MockBean
    private JobsCommandLineRunner jobsCommandLineRunner;

    @Autowired
    private AccessibilityGraphHopperNetworkService accessibilityGraphHopperNetworkService;

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

        NetworkGraphHopper networkGraphHopper = accessibilityGraphHopperNetworkService.loadFromDisk(
                RoutingNetwork.builder().networkNameAndVersion(ACCESSIBILITY_LATEST).build(), graphHopperPath);
        assertThat(networkGraphHopper).isNotNull();
        assertThat(networkGraphHopper.getImportDate()).isNotNull();
        assertThat(networkGraphHopper.getDataDate()).isEqualTo(EXPECTED_DATA_DATE);
    }
}
