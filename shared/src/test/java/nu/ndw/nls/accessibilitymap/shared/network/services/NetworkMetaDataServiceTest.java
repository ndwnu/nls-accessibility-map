package nu.ndw.nls.accessibilitymap.shared.network.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkMetaDataServiceTest {

    private static final String EXPECT_FILE_PATH_STRING = "src/test/resources/network-meta-data-expected-value.json";
    private static final File EXPECTED_CONTENT = new File(EXPECT_FILE_PATH_STRING);
    @Mock
    private GraphHopperConfiguration graphHopperConfiguration;

    @InjectMocks
    private NetworkMetaDataService networkMetaDataService;

    @Test
    @SneakyThrows
    void loadMetaData_ok() {
        when(graphHopperConfiguration.getMetaDataPath()).thenReturn(Path.of(EXPECT_FILE_PATH_STRING));
        assertEquals(new AccessibilityGraphhopperMetaData(20241231), networkMetaDataService.loadMetaData());
    }

    @Test
    @SneakyThrows
    void saveMetaData() {
        Path tempFile = Files.createTempFile("meta-data", ".json");
        when(graphHopperConfiguration.getMetaDataPath()).thenReturn(tempFile);

        networkMetaDataService.saveMetaData(new AccessibilityGraphhopperMetaData(20241231));
        assertTrue(Files.exists(tempFile));
        FileUtils.contentEquals(EXPECTED_CONTENT, tempFile.toFile());
    }
}