package nu.ndw.nls.accessibilitymap.jobs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles(profiles = {"integration-test"})
public class NlsAccessibilityMapJobsIT {

    // Mocking this bean to prevent stderr output about missing PicoCLI commands when running IT
    @MockBean
    private JobsCommandLineRunner jobsCommandLineRunner;

    @Value("${graphhopper.dir}")
    private String graphHopperDir;

    @Test
    void createOrUpdateNetwork_ok() {
        Path path = Path.of(graphHopperDir).resolve("accessibility_latest");
        assertTrue(Files.exists(path));
        // Check whether network is fully built.
        assertTrue(Files.exists(path.resolve("properties")));
    }
}
