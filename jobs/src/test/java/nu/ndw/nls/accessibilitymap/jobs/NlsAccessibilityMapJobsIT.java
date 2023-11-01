package nu.ndw.nls.accessibilitymap.jobs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles(profiles = {"integration-test"})
public class NlsAccessibilityMapJobsIT {

    // Mocking this bean to prevent stderr output about missing PicoCLI commands when running IT
    @MockBean
    private JobsCommandLineRunner jobsCommandLineRunner;

    @Test
    void createOrUpdateNetwork_ok() {
        Path path = Path.of("..", "graphhopper", "nwb_latest");
        assertTrue(Files.exists(path));
        // Check whether network is fully built.
        assertTrue(Files.exists(path.resolve("properties")));
    }
}
