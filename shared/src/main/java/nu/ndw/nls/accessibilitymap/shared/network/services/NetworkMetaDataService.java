package nu.ndw.nls.accessibilitymap.shared.network.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NetworkMetaDataService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GraphHopperConfiguration graphHopperConfiguration;

    public AccessibilityGraphhopperMetaData loadMetaData() {
        Path latestPath = graphHopperConfiguration.getMetaDataPath();
        try {
            return objectMapper.readValue(latestPath.toFile(), AccessibilityGraphhopperMetaData.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load meta-data from file path: " + latestPath, e);
        }
    }

    public void saveMetaData(AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData) {
        Path latestPath = graphHopperConfiguration.getMetaDataPath();
        try {
            objectMapper.writeValue(latestPath.toFile(), accessibilityGraphhopperMetaData);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write meta-data from file path: " + latestPath, e);
        }
    }

}
