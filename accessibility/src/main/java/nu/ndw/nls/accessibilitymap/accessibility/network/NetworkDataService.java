package nu.ndw.nls.accessibilitymap.accessibility.network;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.network.configuration.NetworkCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
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

    private final NwbVersionCrudService nwbVersionCrudService;

    public NetworkDataService(
            NetworkCacheConfiguration networkCacheConfiguration,
            ClockService clockService,
            DistributedLockService distributedLockService,
            GraphHopperService graphHopperService,
            AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService,
            ObjectMapper objectMapper, NwbVersionCrudService nwbVersionCrudService
    ) {

        super(networkCacheConfiguration, clockService, distributedLockService);

        this.objectMapper = objectMapper;
        this.graphHopperService = graphHopperService;
        this.accessibilityNwbRoadSectionService = accessibilityNwbRoadSectionService;
        this.nwbVersionCrudService = nwbVersionCrudService;
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
        NwbDataUpdates nwbDataUpdates;
        if (nwbDataUpdatesExists()) {
            try {
                nwbDataUpdates = readNwbDataUpdates();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            Integer nwbVersionId = nwbVersionCrudService.findLatestVersionId();
            nwbDataUpdates = new NwbDataUpdates(Objects.isNull(nwbVersionId) ? -1 : nwbVersionId, List.of());
        }
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

    private boolean nwbDataUpdatesExists() {
        return getCacheConfiguration().getActiveVersion().toPath()
                .resolve(NWB_UPDATES_ROAD_SECTIONS_JSON).toFile().exists();
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
        Path roadSectionsFile = nwbPath.resolve("roadSections.json");

        if (!Files.exists(roadSectionsFile)) {
            Files.createDirectories(nwbPath);
            Files.createFile(roadSectionsFile);
        }

        try (JsonGenerator gen = objectMapper
                .getFactory()
                .createGenerator(Files.newOutputStream(target.resolve(Path.of(NWB_ROAD_SECTIONS_JSON))))) {

            objectMapper.writeValue(gen, data.getNwbData());
        }

//        FileUtils.writeStringToFile(
//                target.resolve(Path.of(NWB_ROAD_SECTIONS_JSON)).toFile(),
//                objectMapper.writeValueAsString(data.getNwbData()),
//                StandardCharsets.UTF_8);

        FileUtils.writeStringToFile(
                target.resolve(Path.of(NWB_UPDATES_ROAD_SECTIONS_JSON)).toFile(),
                objectMapper.writeValueAsString(data.getNwbDataUpdates()),
                StandardCharsets.UTF_8);

        graphHopperService.save(
                target.resolve(GRAPH_HOPPER_FOLDER),
                data.getNwbData());
    }
}
