package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.AccessibilityNwbRoadSectionToLinkMapper;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.Link;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperServiceTest {

    private GraphHopperService graphHopperService;

    @Mock
    private GraphHopperNetworkService graphHopperNetworkService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private AccessibilityNwbRoadSectionToLinkMapper accessibilityNwbRoadSectionToLinkMapper;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ClockService clockService;

    @Mock
    private GraphHopperNotImportedException graphHopperNotImportedException;

    @Mock
    private NwbData nwbData;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection;

    @Mock
    private AccessibilityLink accessibilityLink;

    @Mock
    private IOException ioException;

    private Path testDir;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory("testDir");
        graphHopperService = new GraphHopperService(
                graphHopperNetworkService,
                accessibilityNwbRoadSectionToLinkMapper,
                objectMapper,
                clockService);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void load() throws GraphHopperNotImportedException, IOException {

        Path graphHopperDir = testDir.resolve("latest");
        Path metaDataFile = graphHopperDir.resolve("accessibility_metadata.json");

        Files.createDirectories(graphHopperDir);
        Files.writeString(metaDataFile, "{}");

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2023-09-25T12:00:00Z"))
                .thenReturn(OffsetDateTime.parse("2023-09-25T12:00:01.234Z"));

        when(graphHopperNetworkService.loadFromDisk(assertArg(routingNetworkSettings ->
                assertThat(routingNetworkSettings).isEqualTo(RoutingNetworkSettings.builder(AccessibilityLink.class)
                        .indexed(true)
                        .graphhopperRootPath(testDir)
                        .networkNameAndVersion("latest")
                        .profiles(List.of(CAR_PROFILE))
                        .build())))).thenReturn(networkGraphHopper);
        when(objectMapper.readValue(metaDataFile.toFile(), GraphhopperMetaData.class)).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(1);

        GraphHopperNetwork graphHopperNetwork = graphHopperService.load(testDir);

        assertThat(graphHopperNetwork.network()).isEqualTo(networkGraphHopper);
        assertThat(graphHopperNetwork.nwbVersion()).isEqualTo(1);

        loggerExtension.containsLog(Level.INFO, "GraphHopper network loaded from disk in 1234ms");
        assertThat(testDir.resolve("latest"))
                .exists()
                .isDirectory();
    }

    @Test
    void load_graphHopperNotImported() throws GraphHopperNotImportedException, IOException {

        Path graphHopperDir = testDir.resolve("latest");
        Path metaDataFile = graphHopperDir.resolve("accessibility_metadata.json");

        Files.createDirectories(graphHopperDir);
        Files.writeString(metaDataFile, "{}");

        when(clockService.now()).thenReturn(OffsetDateTime.parse("2023-09-25T12:00:00Z"));

        when(graphHopperNetworkService.loadFromDisk(any())).thenThrow(graphHopperNotImportedException);

        assertThat(catchThrowable(() -> graphHopperService.load(testDir)))
                .hasMessage("Could not load GraphHopper network from %s".formatted(testDir.toAbsolutePath()))
                .hasCause(graphHopperNotImportedException)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void load_loadMetaDataError() throws IOException, GraphHopperNotImportedException {

        Path graphHopperDir = testDir.resolve("latest");
        Path metaDataFile = graphHopperDir.resolve("accessibility_metadata.json");

        Files.createDirectories(graphHopperDir);
        Files.writeString(metaDataFile, "{}");

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2023-09-25T12:00:00Z"))
                .thenReturn(OffsetDateTime.parse("2023-09-25T12:00:01.234Z"));

        when(graphHopperNetworkService.loadFromDisk(assertArg(routingNetworkSettings ->
                assertThat(routingNetworkSettings).isEqualTo(RoutingNetworkSettings.builder(AccessibilityLink.class)
                        .indexed(true)
                        .graphhopperRootPath(testDir)
                        .networkNameAndVersion("latest")
                        .profiles(List.of(CAR_PROFILE))
                        .build())))).thenReturn(networkGraphHopper);

        doThrow(ioException).when(objectMapper).readValue(metaDataFile.toFile(), GraphhopperMetaData.class);

        assertThat(catchThrowable(() -> graphHopperService.load(testDir)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not load meta-data from file path: %s".formatted(metaDataFile.toAbsolutePath()))
                .hasCause(ioException);
    }

    @Test
    void save() throws IOException {

        Path graphHopperDir = testDir.resolve("latest");
        Path metaDataFile = graphHopperDir.resolve("accessibility_metadata.json");

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2023-09-25T12:00:00Z"))
                .thenReturn(OffsetDateTime.parse("2023-09-25T12:00:01.234Z"));

        when(nwbData.getNwbVersionId()).thenReturn(1);
        when(nwbData.findAllAccessibilityNwbRoadSections()).thenReturn(List.of(accessibilityNwbRoadSection));
        when(accessibilityNwbRoadSectionToLinkMapper.map(accessibilityNwbRoadSection)).thenReturn(accessibilityLink);

        graphHopperService.save(testDir, nwbData);

        verify(graphHopperNetworkService).storeOnDisk(assertArg(routingNetworkSettings -> {

            assertThat(routingNetworkSettings.isIndexed()).isTrue();
            assertThat(routingNetworkSettings.getGraphhopperRootPath()).isEqualTo(testDir);
            assertThat(routingNetworkSettings.getNetworkNameAndVersion()).isEqualTo("latest");
            assertThat(routingNetworkSettings.getProfiles()).containsExactly(CAR_PROFILE);

            Iterator<Link> linkSupplierIterator = routingNetworkSettings.getLinkSupplier().get();
            assertThat(linkSupplierIterator.hasNext()).isTrue();
            assertThat(linkSupplierIterator.next()).isEqualTo(accessibilityLink);
            assertThat(linkSupplierIterator.hasNext()).isFalse();
            assertThat(routingNetworkSettings.getDataDate()).isEqualTo(OffsetDateTime.parse("2023-09-25T12:00:00Z").toInstant());
        }));
        verify(objectMapper).writeValue(metaDataFile.toFile(), new GraphhopperMetaData(1));

        loggerExtension.containsLog(Level.INFO, "GraphHopper network stored on disk in 1234ms");
        assertThat(graphHopperDir)
                .exists()
                .isDirectory();
    }

    @Test
    void save_saveMetaDataError() throws IOException {

        Path graphHopperDir = testDir.resolve("latest");
        Path metaDataFile = graphHopperDir.resolve("accessibility_metadata.json");

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2023-09-25T12:00:00Z"))
                .thenReturn(OffsetDateTime.parse("2023-09-25T12:00:01.234Z"));

        when(nwbData.getNwbVersionId()).thenReturn(1);
        when(nwbData.findAllAccessibilityNwbRoadSections()).thenReturn(List.of(accessibilityNwbRoadSection));
        when(accessibilityNwbRoadSectionToLinkMapper.map(accessibilityNwbRoadSection)).thenReturn(accessibilityLink);

        doThrow(ioException).when(objectMapper).writeValue(metaDataFile.toFile(), new GraphhopperMetaData(1));

        assertThat(catchThrowable(() -> graphHopperService.save(testDir, nwbData)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not write meta-data to file path: %s".formatted(metaDataFile.toAbsolutePath()))
                .hasCause(ioException);

        assertThat(graphHopperDir)
                .exists()
                .isDirectory();
    }
}
