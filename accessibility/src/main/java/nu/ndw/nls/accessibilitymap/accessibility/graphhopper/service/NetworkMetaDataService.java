package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperNetworkSettingsBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NetworkMetaDataService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    public GraphhopperMetaData loadMetaData() {
        Path latestPath = graphHopperNetworkSettingsBuilder.getMetadataPath();
        try {
            return objectMapper.readValue(latestPath.toFile(), GraphhopperMetaData.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load meta-data from file path: " + latestPath, e);
        }
    }

    public void saveMetaData(GraphhopperMetaData graphhopperMetaData) {
        Path latestPath = graphHopperNetworkSettingsBuilder.getMetadataPath();
        try {
            objectMapper.writeValue(latestPath.toFile(), graphhopperMetaData);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write meta-data to file path: " + latestPath, e);
        }
    }

}
