package nu.ndw.nls.accessibilitymap.accessibility.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheLoadedEvent;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheLoadedEvent.Type;
import nu.ndw.nls.accessibilitymap.accessibility.cache.DataStaleException;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonNwbDataStreamReaderWriter;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonWriter;
import nu.ndw.nls.accessibilitymap.accessibility.network.configuration.NetworkCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NetworkDataService extends Cache<NetworkData> {

    public static final String GRAPH_HOPPER_FOLDER = "graphHopper";

    public static final String NWB_ROAD_SECTIONS_JSON = "nwb/roadSections.json";

    private static final String NWB_UPDATES_ROAD_SECTIONS_JSON = "nwbUpdates/nwb_changed_road_sections.json";

    private static final String NWB_UPDATE_DIRECTORY = "nwbUpdates";

    private static final String NWB_CHANGED_ROAD_SECTIONS_FILE = "nwb_changed_road_sections.json";

    private final ObjectMapper objectMapper;

    private final GraphHopperService graphHopperService;

    private final AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    private final JsonWriter jsonWriter;

    private final JsonNwbDataStreamReaderWriter jsonNwbDataStreamReaderWriter;

    private final ApplicationEventPublisher applicationEventPublisher;

    public NetworkDataService(
            NetworkCacheConfiguration networkCacheConfiguration,
            ClockService clockService,
            DistributedLockService distributedLockService,
            GraphHopperService graphHopperService,
            AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService,
            ObjectMapper objectMapper, JsonWriter jsonWriter, JsonNwbDataStreamReaderWriter jsonNwbDataStreamReaderWriter,
            ApplicationEventPublisher applicationEventPublisher
    ) {

        super(networkCacheConfiguration, clockService, distributedLockService);

        this.objectMapper = objectMapper;
        this.graphHopperService = graphHopperService;
        this.accessibilityNwbRoadSectionService = accessibilityNwbRoadSectionService;
        this.jsonWriter = jsonWriter;
        this.jsonNwbDataStreamReaderWriter = jsonNwbDataStreamReaderWriter;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Retryable(
            retryFor = DataStaleException.class,
            maxAttempts = 15,
            backoff = @Backoff(delay = 5000)
    )
    @Transactional
    public void writeNwbDataUpdates(NwbDataUpdates nwbDataUpdates) {

        try {
            if (isDataStale()) {
                log.warn("NetworkData is stale, not writing to disk waiting 5 seconds");
                throw new DataStaleException("NetworkData is stale");
            }
            getDistributedLockService().lockOrFail(getCacheConfiguration().getName(), getCacheConfiguration().getMaxLockWaitTime());
            NetworkData networkData = get();
            NwbDataUpdates previousChanges = networkData.getNwbDataUpdates();
            NwbDataUpdates newNwbDataUpdates = previousChanges.merge(nwbDataUpdates);
            NetworkData updatedNetworkData = new NetworkData(networkData.getNetworkGraphHopper(),
                    networkData.getNwbData(),
                    newNwbDataUpdates);
            log.debug("NwbDataUpdates merged: {}", newNwbDataUpdates);

            OffsetDateTime start = getClockService().now();
            Path targetFolder = Path.of(start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            Path targetLocation = getCacheConfiguration().getFolder().resolve(targetFolder);
            Files.createDirectories(targetLocation);
            Path activeVersion = getCacheConfiguration().getActiveVersion().toPath().toAbsolutePath().toRealPath();
            boolean preserveFileDate = false;
            FileUtils.copyDirectory(activeVersion.toFile(), targetLocation.toFile(), null, preserveFileDate);
            log.info("Copied active version to {}", targetLocation.toAbsolutePath());

            Path nwbUpdatesPath = targetLocation.resolve(NWB_UPDATE_DIRECTORY);
            jsonWriter.writeJsonToFile(nwbUpdatesPath, NWB_CHANGED_ROAD_SECTIONS_FILE, newNwbDataUpdates);
            log.info("Wrote nwbDataUpdates to {}", nwbUpdatesPath);
            switchSymLink(targetFolder);
            setData(updatedNetworkData, targetLocation);
            log.info("Wrote nwbDataUpdates to disk in {}ms", Duration.between(start, getClockService().now()).toMillis());
        } catch (IOException exception) {
            log.error("Failed to write nwbDataUpdates to disk", exception);
            throw new IllegalStateException(exception);
        } finally {
            getDistributedLockService().unlock(getCacheConfiguration().getName());
        }
    }

    @Recover
    public void recover(DataStaleException exception, NwbDataUpdates nwbDataUpdates) {
        log.error("Retries exhausted while writing nwbDataUpdates {} to disk", nwbDataUpdates, exception);
        throw exception;
    }

    @Transactional
    public void recompileData() {

        NwbData nwbData = accessibilityNwbRoadSectionService.getLatestNwbData();

        // Create empty nwbDataUpdates for a new map-version
        Integer nwbVersionId = nwbData.getNwbVersionId();
        NwbDataUpdates nwbDataUpdates = new NwbDataUpdates(nwbVersionId, List.of());

        write(() -> new NetworkData(
                GraphHopperNetwork.builder()
                        .nwbVersion(nwbData.getNwbVersionId())
                        .build(),
                nwbData, nwbDataUpdates));
    }

    @Override
    protected NetworkData readData(Path activeVersion) throws IOException {
        OffsetDateTime start = getClockService().now();
        NwbData nwbData = readNwbData();
        NwbDataUpdates nwbDataUpdates = readNwbDataUpdates();
        log.info("Nwb road sections loaded from disk in {}ms", Duration.between(start, getClockService().now()).toMillis());

        GraphHopperNetwork graphHopperNetwork = graphHopperService.load(
                getCacheConfiguration().getActiveVersion().toPath().resolve(GRAPH_HOPPER_FOLDER));

        return new NetworkData(
                graphHopperNetwork,
                nwbData, nwbDataUpdates);
    }

    private NwbDataUpdates readNwbDataUpdates() throws IOException {
        return objectMapper.readValue(
                getCacheConfiguration().getActiveVersion().toPath().resolve(NWB_UPDATES_ROAD_SECTIONS_JSON).toFile(),
                NwbDataUpdates.class);
    }

    private NwbData readNwbData() {
        final Path nwbDataFilePath = getCacheConfiguration().getActiveVersion().toPath().resolve(NWB_ROAD_SECTIONS_JSON);
        return jsonNwbDataStreamReaderWriter.readJsonData(nwbDataFilePath);
    }

    @Override
    protected void writeData(Path target, NetworkData data) throws IOException {
        Path nwbPath = target.resolve("nwb");
        Path nwbUpdatesPath = target.resolve(NWB_UPDATE_DIRECTORY);
        jsonWriter.writeJsonToFile(nwbPath, "roadSections.json", data.getNwbData());
        jsonWriter.writeJsonToFile(nwbUpdatesPath, NWB_CHANGED_ROAD_SECTIONS_FILE, data.getNwbDataUpdates());
        graphHopperService.save(
                target.resolve(GRAPH_HOPPER_FOLDER),
                data.getNwbData());
    }

    @Override
    protected void publishCacheLoadedEvent() {
        applicationEventPublisher.publishEvent(CacheLoadedEvent.builder().type(Type.NETWORK_DATA)
                .build());
    }
}
