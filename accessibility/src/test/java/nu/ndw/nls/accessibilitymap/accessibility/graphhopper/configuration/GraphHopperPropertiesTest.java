package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperPropertiesTest {

    private Path testDir;

    private GraphHopperProperties graphHopperProperties;

    @BeforeEach
    void setUp() throws IOException {
        testDir = Files.createTempDirectory("testDir");

        graphHopperProperties = new GraphHopperProperties(testDir.resolve("dir"), "network_name", true);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void getLatestPath() {

        assertThat(graphHopperProperties.getLatestPath()).isEqualTo(testDir.resolve("dir").resolve("network_name"));
    }

    @Test
    void getMetadataPath() {

        assertThat(graphHopperProperties.getMetadataPath()).isEqualTo(
                testDir.resolve("dir").resolve("network_name").resolve("accessibility_metadata.json"));
    }
}