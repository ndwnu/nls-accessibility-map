package nu.ndw.nls.accessibilitymap.accessibility.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonWriter;
import nu.ndw.nls.accessibilitymap.accessibility.network.configuration.NetworkCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NetworkDataService extends Cache<NetworkData> {

    public static final String GRAPH_HOPPER_FOLDER = "graphHopper";

    public static final String NWB_ROAD_SECTIONS_JSON = "nwb/roadSections.json";

    private static final String NWB_UPDATES_ROAD_SECTIONS_JSON = "nwbUpdates/nwb_changed_road_sections.json";

    private final ObjectMapper objectMapper;

    private final GraphHopperService graphHopperService;

    private final AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    private final JsonWriter jsonWriter;

    public NetworkDataService(
            NetworkCacheConfiguration networkCacheConfiguration,
            ClockService clockService,
            DistributedLockService distributedLockService,
            GraphHopperService graphHopperService,
            AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService,
            ObjectMapper objectMapper, JsonWriter jsonWriter
    ) {

        super(networkCacheConfiguration, clockService, distributedLockService);

        this.objectMapper = objectMapper;
        this.graphHopperService = graphHopperService;
        this.accessibilityNwbRoadSectionService = accessibilityNwbRoadSectionService;
        this.jsonWriter = jsonWriter;
    }

    @Transactional
    public void writeNwbDataUpdates(NwbDataUpdates nwbDataUpdates) {

        NetworkData networkData = get();
        NwbDataUpdates previousChanges = networkData.getNwbDataUpdates();
        NwbDataUpdates newNwbDataUpdates = previousChanges.merge(nwbDataUpdates);
        super.write(new NetworkData(networkData.getNetworkGraphHopper(), networkData.getNwbData(), newNwbDataUpdates));
    }

    @Transactional
    public void recompileData() {

        NwbData nwbData = accessibilityNwbRoadSectionService.getLatestNwbData();

        // Create empty nwbDataUpdates for a new map-version
        Integer nwbVersionId = nwbData.getNwbVersionId();
        NwbDataUpdates nwbDataUpdates = new NwbDataUpdates(nwbVersionId, List.of());

        write(new NetworkData(
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

    private NwbData readNwbData() throws IOException {
        return objectMapper.readValue(
                getCacheConfiguration().getActiveVersion().toPath().resolve(NWB_ROAD_SECTIONS_JSON).toFile(),
                NwbData.class);
    }

    @Override
    protected void writeData(Path target, NetworkData data) throws IOException {
        Path nwbPath = target.resolve("nwb");
        Path nwbUpdatesPath = target.resolve("nwbUpdates");
        jsonWriter.writeJsonToFile(target, nwbPath, "roadSections.json", data.getNwbData());
        jsonWriter.writeJsonToFile(target, nwbUpdatesPath, "nwb_changed_road_sections.json", data.getNwbDataUpdates());
        graphHopperService.save(
                target.resolve(GRAPH_HOPPER_FOLDER),
                data.getNwbData());
    }
}
