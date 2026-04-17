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
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonWriter;
import nu.ndw.nls.accessibilitymap.accessibility.network.configuration.NetworkCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private DistributedLockService distributedLockService;

    private NwbData nwbData;

    private NwbDataUpdates nwbDataUpdates;

    @BeforeEach
    void setUp() throws IOException {

        objectMapper = new ObjectMapper();
        JsonWriter jsonWriter = new JsonWriter(objectMapper);
        nwbData = new NwbData(1, buildAccessibilityRoadSections());
        nwbDataUpdates = new NwbDataUpdates(1, buildAccessibilityRoadSectionUpdates());
        testDir = Files.createTempDirectory(this.getClass().getSimpleName());
        networkCacheConfiguration = NetworkCacheConfiguration.builder()
                .folder(testDir)
                .name("testCache")
                .build();

        networkDataService = new NetworkDataService(
                networkCacheConfiguration,
                clockService,
                distributedLockService,
                graphHopperService,
                accessibilityNwbRoadSectionService,
                objectMapper, jsonWriter);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @SneakyThrows
    @Test
    void writeNwbDataUpdates() {
        var updatedRoaSections = List.of(
                new AccessibilityNwbRoadSectionUpdate(
                        124,
                        true,
                        false,
                        CarriagewayTypeCode.HR)
        );

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(graphHopperNetwork.nwbVersion()).thenReturn(1);

        NetworkData networkData = new NetworkData(graphHopperNetwork, nwbData, nwbDataUpdates);
        networkDataService.write(() -> networkData);
        networkDataService.writeNwbDataUpdates(new NwbDataUpdates(1, updatedRoaSections));

        NetworkData updatedNetworkData = networkDataService.get();

        assertThat(updatedNetworkData.getNwbDataUpdates()
                .getAccessibilityNwbRoadSectionUpdates()).isEqualTo(List.of(new AccessibilityNwbRoadSectionUpdate(
                123,
                false,
                true,
                CarriagewayTypeCode.RB), new AccessibilityNwbRoadSectionUpdate(
                124,
                true,
                false,
                CarriagewayTypeCode.HR)));
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

    @SneakyThrows
    @Test
    void write() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(graphHopperNetwork.nwbVersion()).thenReturn(1);
        NetworkData networkData = new NetworkData(graphHopperNetwork, nwbData, nwbDataUpdates);
        networkDataService.write(() -> networkData);

        NetworkData actualNetworkData = networkDataService.get();

        assertThat(networkData).isNotNull();
        assertThat(networkData.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(actualNetworkData.getNwbData()).isEqualTo(nwbData);
        assertThat(actualNetworkData.getNwbDataUpdates()).isEqualTo(nwbDataUpdates);

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
        Path nwbUpdatesDir = networkCacheConfiguration.getActiveVersion().toPath().resolve("nwbUpdates");
        Files.createDirectories(nwbUpdatesDir);
        Files.writeString(nwbUpdatesDir.resolve("nwb_changed_road_sections.json"), """
                {"nwbVersionId":1,"changedNwbRoadSections": %s}
                """.formatted(objectMapper.writeValueAsString(buildAccessibilityRoadSectionUpdates())));

        when(graphHopperService.load(networkCacheConfiguration.getActiveVersion().toPath().resolve("graphHopper")))
                .thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.nwbVersion()).thenReturn(1);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);

        networkDataService.read();

        NetworkData networkData = networkDataService.get();

        assertThat(networkData).isNotNull();
        assertThat(networkData.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(networkData.getNwbData().getNwbVersionId()).isEqualTo(nwbData.getNwbVersionId());
        assertThat(networkData.getNwbData().getAccessibilityNwbRoadSections()).isEqualTo(buildAccessibilityRoadSections());
        assertThat(networkData.getNwbDataUpdates().getNwbVersionId()).isEqualTo(nwbDataUpdates.getNwbVersionId());
        assertThat(networkData.getNwbDataUpdates()
                .getAccessibilityNwbRoadSectionUpdates()).isEqualTo(buildAccessibilityRoadSectionUpdates());
    }

    @Test
    void dataExists() throws IOException {

        Files.createDirectories(networkCacheConfiguration.getActiveVersion().toPath());

        assertThat(networkDataService.dataExists()).isTrue();
    }

    @Test
    void dataExists_doesNotExist() {

        assertThat(networkDataService.dataExists()).isFalse();
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
                        false,
                        CarriagewayTypeCode.RB,
                        "1")
        );
    }

    private static List<AccessibilityNwbRoadSectionUpdate> buildAccessibilityRoadSectionUpdates() {
        return List.of(
                new AccessibilityNwbRoadSectionUpdate(
                        123,
                        false,
                        true,
                        CarriagewayTypeCode.RB)
        );
    }
}
