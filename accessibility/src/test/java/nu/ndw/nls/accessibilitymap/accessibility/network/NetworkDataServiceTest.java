package nu.ndw.nls.accessibilitymap.accessibility.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.network.configuration.NetworkCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkDataServiceTest {

    private NetworkDataService networkDataService;

    @Mock
    private GraphHopperService graphHopperService;

    @Mock
    private AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    private NetworkCacheConfiguration networkCacheConfiguration;

    @Mock
    private ClockService clockService;

    private Path testDir;

    private ObjectMapper objectMapper;

    private GraphHopperNetwork graphHopperNetwork;

    private NwbData nwbData;

    @BeforeEach
    void setUp() throws IOException {

        objectMapper = new ObjectMapper();
        graphHopperNetwork = GraphHopperNetwork.builder()
                .nwbVersion(1)
                .network(null)
                .build();

        nwbData = new NwbData(1, buildAccessibilityRoadSections());

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());
        networkCacheConfiguration = NetworkCacheConfiguration.builder()
                .folder(testDir)
                .name("testCache")
                .build();

        networkDataService = new NetworkDataService(
                networkCacheConfiguration,
                clockService,
                graphHopperService,
                accessibilityNwbRoadSectionService,
                objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void recompileData() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        when(accessibilityNwbRoadSectionService.getLatestNwbData()).thenReturn(nwbData);

        networkDataService.recompileData();
        NetworkData networkData = networkDataService.get();

        assertThat(networkData).isNotNull();
    }

    @Test
    void write() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        NetworkData networkData = new NetworkData(graphHopperNetwork, nwbData);
        networkDataService.write(networkData);

        NetworkData actualNetworkData = networkDataService.get();

        assertThat(networkData).isNotNull();
        assertThat(networkData.getGraphHopperNetwork()).isEqualTo(graphHopperNetwork);
        assertThat(actualNetworkData.getNwbData()).isEqualTo(nwbData);
    }

    @Test
    void read() throws IOException {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Path nwbDir = networkCacheConfiguration.getActiveVersion().toPath().resolve("nwb");
        Files.createDirectories(nwbDir);
        Files.writeString(
                nwbDir.resolve("roadSections.json"), """
                        {
                           "nwbVersionId": 1,
                           "accessibilityNwbRoadSections": %s
                        }
                        """.formatted(objectMapper.writeValueAsString(buildAccessibilityRoadSections())));

        when(graphHopperService.load(networkCacheConfiguration.getActiveVersion().toPath().resolve("graphHopper")))
                .thenReturn(graphHopperNetwork);

        networkDataService.read();

        NetworkData networkData = networkDataService.get();

        NetworkData actualNetworkData = networkDataService.get();

        assertThat(networkData).isNotNull();
        assertThat(networkData.getGraphHopperNetwork()).isEqualTo(graphHopperNetwork);
        assertThat(actualNetworkData.getNwbData().getNwbVersionId()).isEqualTo(nwbData.getNwbVersionId());
        assertThat(actualNetworkData.getNwbData().getAccessibilityNwbRoadSections()).isEqualTo(buildAccessibilityRoadSections());
    }

    @Test
    void networkExists() throws IOException {

        Files.createDirectories(networkCacheConfiguration.getActiveVersion().toPath());

        assertThat(networkDataService.networkExists()).isTrue();
    }

    @Test
    void networkExists_doesNotExist() {

        assertThat(networkDataService.networkExists()).isFalse();
    }

    private static List<AccessibilityNwbRoadSection> buildAccessibilityRoadSections() {
        GeometryFactory geometryFactory = new GeometryFactory();

        LineString lineString = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(12.3, 12.4),
                new Coordinate(22.3, 22.4)
        });

        return List.of(
                new AccessibilityNwbRoadSection(
                        1L,
                        2L,
                        3L,
                        4,
                        lineString,
                        true,
                        false)
        );
    }
}
