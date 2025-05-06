package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphhopperConfigurationTest {

    private GraphhopperConfiguration accessibilityConfiguration;

    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        testDir = Files.createTempDirectory("testDir");

        accessibilityConfiguration = new GraphhopperConfiguration(networkMetaDataService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    @SneakyThrows
    void getMetaData() {

        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);

        assertThat(accessibilityConfiguration.getMetaData()).isEqualTo(graphhopperMetaData);
    }

    @Test
    void edgeIteratorStateReverseExtractor() {

        assertThat(accessibilityConfiguration.edgeIteratorStateReverseExtractor()).isNotNull();
    }
}