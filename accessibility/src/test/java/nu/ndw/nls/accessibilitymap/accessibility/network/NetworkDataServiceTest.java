package nu.ndw.nls.accessibilitymap.accessibility.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheLoadedEvent;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheLoadedEvent.Type;
import nu.ndw.nls.accessibilitymap.accessibility.cache.active.ActiveVersionRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.retry.RetryTemplate;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class NetworkDataServiceTest {

    private static final String TEST_CACHE_NAME = "testCache";

    private static final Duration MAX_LOCK_WAIT_TIME = Duration.ofSeconds(10);

    private static final String INITIAL_TIMESTAMP = "2022-03-11T09:03:01.123-01:00";

    private static final String UPDATED_TIMESTAMP = "2022-03-11T09:03:01.433-01:00";

    private NetworkDataService networkDataService;

    @Mock
    private GraphHopperService graphHopperService;

    @Mock
    private AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    private NetworkCacheConfiguration networkCacheConfiguration;

    @Mock
    private ClockService clockService;

    private Path testDir;

    private JsonMapper jsonMapper;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private DistributedLockService distributedLockService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private ActiveVersionRepository activeVersionRepository;

    @Mock
    private RetryTemplate testRetryTemplate;


    @Captor
    private ArgumentCaptor<CacheLoadedEvent> cacheLoadedEventCaptor;

    private NwbData nwbData;

    private NwbDataUpdates nwbDataUpdates;

    @BeforeEach
    void setUp() throws IOException {

        jsonMapper = new JsonMapper();
        JsonWriter jsonWriter = new JsonWriter(jsonMapper);
        nwbData = new NwbData(1, buildAccessibilityRoadSections());
        nwbDataUpdates = new NwbDataUpdates(1, buildAccessibilityRoadSectionUpdates());
        testDir = Files.createTempDirectory(this.getClass().getSimpleName());
        networkCacheConfiguration = NetworkCacheConfiguration.builder()
                .folder(testDir)
                .name(TEST_CACHE_NAME)
                .maxLockWaitTime(MAX_LOCK_WAIT_TIME)
                .build();

        networkDataService = new NetworkDataService(
                networkCacheConfiguration,
                clockService,
                distributedLockService,
                graphHopperService,
                accessibilityNwbRoadSectionService,
                jsonMapper,
                jsonWriter,
                applicationEventPublisher,
                activeVersionRepository,
                testRetryTemplate);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @SneakyThrows
    @Test
    void writeNwbDataUpdates() {
        var updatedRoadSections = List.of(
                new AccessibilityNwbRoadSectionUpdate(
                        124,
                        true,
                        false,
                        CarriagewayTypeCode.HR)
        );
        when(activeVersionRepository.findActiveVersion(TEST_CACHE_NAME))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(INITIAL_TIMESTAMP))
                .thenReturn(Optional.of(INITIAL_TIMESTAMP))
                .thenReturn(Optional.of(INITIAL_TIMESTAMP))
                .thenReturn(Optional.of(UPDATED_TIMESTAMP));
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(INITIAL_TIMESTAMP, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(UPDATED_TIMESTAMP, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(graphHopperNetwork.nwbVersion()).thenReturn(1);

        NetworkData networkData = new NetworkData(graphHopperNetwork, nwbData, nwbDataUpdates);
        networkDataService.write(() -> networkData);
        networkDataService.writeNwbDataUpdates(new NwbDataUpdates(1, updatedRoadSections));

        NetworkData updatedNetworkData = networkDataService.get();

        assertThat(updatedNetworkData.getNwbNetworkData().getNwbDataUpdates()
                .getAccessibilityNwbRoadSectionUpdates()).isEqualTo(List.of(new AccessibilityNwbRoadSectionUpdate(
                123,
                false,
                true,
                CarriagewayTypeCode.RB), new AccessibilityNwbRoadSectionUpdate(
                124,
                true,
                false,
                CarriagewayTypeCode.HR)));

        verify(distributedLockService, times(2)).lockOrFail(TEST_CACHE_NAME, MAX_LOCK_WAIT_TIME);
        verify(distributedLockService, times(2)).unlock(TEST_CACHE_NAME);
    }

    @Test
    void recompileData() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(INITIAL_TIMESTAMP, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(UPDATED_TIMESTAMP, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(activeVersionRepository.findActiveVersion(TEST_CACHE_NAME)).thenReturn(Optional.of(UPDATED_TIMESTAMP));
        when(accessibilityNwbRoadSectionService.getLatestNwbData()).thenReturn(nwbData);

        networkDataService.recompileData();
        NetworkData networkData = networkDataService.get();

        assertThat(networkData).isNotNull();
    }

    @SneakyThrows
    @Test
    void write() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(INITIAL_TIMESTAMP, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse(UPDATED_TIMESTAMP, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(activeVersionRepository.findActiveVersion(TEST_CACHE_NAME)).thenReturn(Optional.of(UPDATED_TIMESTAMP));
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(graphHopperNetwork.nwbVersion()).thenReturn(1);
        NetworkData networkData = new NetworkData(graphHopperNetwork, nwbData, nwbDataUpdates);
        networkDataService.write(() -> networkData);

        NetworkData actualNetworkData = networkDataService.get();

        assertThat(networkData).isNotNull();
        assertThat(networkData.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(actualNetworkData.getNwbNetworkData().getNwbData()).isEqualTo(nwbData);
        assertThat(actualNetworkData.getNwbNetworkData().getNwbDataUpdates()).isEqualTo(nwbDataUpdates);
    }

    @Test
    void read() throws IOException {
        when(activeVersionRepository.findActiveVersion(TEST_CACHE_NAME))
                .thenReturn(Optional.of(INITIAL_TIMESTAMP));

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse(INITIAL_TIMESTAMP, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Path nwbDir = networkCacheConfiguration.getFolder().resolve(INITIAL_TIMESTAMP).resolve("nwb");

        Files.createDirectories(nwbDir);
        Files.writeString(
                nwbDir.resolve("roadSections.json"), """
                        {
                           "nwbVersionId": 1,
                           "accessibilityNwbRoadSections": %s
                        }
                        """.formatted(jsonMapper.writeValueAsString(buildAccessibilityRoadSections())));
        Path nwbUpdatesDir = networkCacheConfiguration.getFolder().resolve(INITIAL_TIMESTAMP).resolve("nwbUpdates");
        Files.createDirectories(nwbUpdatesDir);
        Files.writeString(nwbUpdatesDir.resolve("nwb_changed_road_sections.json"), """
                {"nwbVersionId":1,"changedNwbRoadSections": %s}
                """.formatted(jsonMapper.writeValueAsString(buildAccessibilityRoadSectionUpdates())));

        when(graphHopperService.load(networkCacheConfiguration.getFolder().resolve(INITIAL_TIMESTAMP).resolve("graphHopper")))
                .thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.nwbVersion()).thenReturn(1);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);

        networkDataService.read();

        NetworkData networkData = networkDataService.get();

        assertThat(networkData).isNotNull();
        assertThat(networkData.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(networkData.getNwbNetworkData().getNwbData().getNwbVersionId()).isEqualTo(nwbData.getNwbVersionId());
        assertThat(networkData.getNwbNetworkData().getNwbData().getAccessibilityNwbRoadSections()).isEqualTo(buildAccessibilityRoadSections());
        assertThat(networkData.getNwbNetworkData().getNwbDataUpdates().getNwbVersionId()).isEqualTo(nwbDataUpdates.getNwbVersionId());
        assertThat(networkData.getNwbNetworkData().getNwbDataUpdates()
                .getAccessibilityNwbRoadSectionUpdates()).isEqualTo(buildAccessibilityRoadSectionUpdates());
        verify(applicationEventPublisher).publishEvent(cacheLoadedEventCaptor.capture());
        assertThat(cacheLoadedEventCaptor.getValue().getType()).isEqualTo(Type.NETWORK_DATA);
    }

    @SneakyThrows
    @Test
    void roadSectionCache_preservesRoadOperatorCode() {

        AccessibilityNwbRoadSection roadSection = buildAccessibilityRoadSections().getFirst();

        String json = jsonMapper.writeValueAsString(roadSection);
        AccessibilityNwbRoadSection deserialized = jsonMapper.readValue(json, AccessibilityNwbRoadSection.class);

        assertThat(json).contains("roadOperatorCode");
        assertThat(deserialized.roadOperatorCode()).isEqualTo("WS14");
    }

    @Test
    void dataExists() throws IOException {
        when(activeVersionRepository.findActiveVersion(TEST_CACHE_NAME))
                .thenReturn(Optional.of(INITIAL_TIMESTAMP));
        Files.createDirectories(networkCacheConfiguration.getFolder().resolve(INITIAL_TIMESTAMP));

        assertThat(networkDataService.dataExists()).isTrue();
    }

    @Test
    void dataExists_doesNotExist() {

        assertThat(networkDataService.dataExists()).isFalse();
    }

    private static List<AccessibilityNwbRoadSection> buildAccessibilityRoadSections() {

        return List.of(
                new AccessibilityNwbRoadSection(
                        1L,
                        2L,
                        3L,
                        4,
                        null,
                        true,
                        false,
                        CarriagewayTypeCode.RB,
                        "1",
                        "WS14")
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
