package nu.ndw.nls.accessibilitymap.accessibility.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.network.configuration.NetworkCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NetworkDataService extends Cache<NetworkData> {

    public static final String GRAPH_HOPPER_FOLDER = "graphHopper";

    public static final String NWB_ROAD_SECTIONS_JSON = "nwb/roadSections.json";

    private final ObjectMapper objectMapper;

    private final GraphHopperService graphHopperService;

    private final AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    private final NetworkCacheConfiguration networkCacheConfiguration;

    public NetworkDataService(
            NetworkCacheConfiguration networkCacheConfiguration,
            ClockService clockService,
            GraphHopperService graphHopperService,
            AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService,
            ObjectMapper objectMapper) {

        super(networkCacheConfiguration, clockService);

        this.networkCacheConfiguration = networkCacheConfiguration;
        this.objectMapper = objectMapper;
        this.graphHopperService = graphHopperService;
        this.accessibilityNwbRoadSectionService = accessibilityNwbRoadSectionService;
    }

    @Transactional
    public void recompileData() {

        NwbData nwbData = accessibilityNwbRoadSectionService.getLatestNwbData();
        write(new NetworkData(
                GraphHopperNetwork.builder()
                        .nwbVersion(nwbData.getNwbVersionId())
                        .build(),
                nwbData));
    }

    @Override
    protected NetworkData readData(Path activeVersion) throws IOException {
        OffsetDateTime start = getClockService().now();
        NwbData nwbData = objectMapper.readValue(
                getCacheConfiguration().getActiveVersion().toPath().resolve(NWB_ROAD_SECTIONS_JSON).toFile(),
                NwbData.class);
        log.info("Nwb road sections loaded from disk in {}ms", Duration.between(start, getClockService().now()).toMillis());

        GraphHopperNetwork graphHopperNetwork = graphHopperService.load(
                getCacheConfiguration().getActiveVersion().toPath().resolve(GRAPH_HOPPER_FOLDER));

        return new NetworkData(
                graphHopperNetwork,
                nwbData);
    }

    @Override
    protected void writeData(Path target, NetworkData data) throws IOException {

        FileUtils.writeStringToFile(
                target.resolve(Path.of(NWB_ROAD_SECTIONS_JSON)).toFile(),
                objectMapper.writeValueAsString(data.getNwbData()),
                StandardCharsets.UTF_8);

        graphHopperService.save(
                target.resolve(GRAPH_HOPPER_FOLDER),
                data.getNwbData());
    }

    public boolean networkExists() {
        return Files.exists(networkCacheConfiguration.getActiveVersion().toPath());
    }
}
