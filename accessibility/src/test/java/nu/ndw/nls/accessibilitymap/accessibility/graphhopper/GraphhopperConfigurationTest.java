package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

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
class GraphhopperConfigurationTest {

    private GraphhopperConfiguration accessibilityConfiguration;

    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        testDir = Files.createTempDirectory("testDir");

        accessibilityConfiguration = new GraphhopperConfiguration();
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void edgeIteratorStateReverseExtractor() {

        assertThat(accessibilityConfiguration.edgeIteratorStateReverseExtractor()).isNotNull();
    }

    @Test
    void algorithmFactory() {

        assertThat(accessibilityConfiguration.algorithmFactory()).isNotNull();
    }
}
