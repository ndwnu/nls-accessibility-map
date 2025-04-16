package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperNetworkSettingsBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
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

    private static final String FILE_DOES_NOT_EXIST = "file-does-not-exist";

    private static final String TMP_DIRECTORY_IS_NOT_A_WRITEABLE_FILE = "tmp-directory-is-not-a-writeable-file";

    @Mock
    private GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    @InjectMocks
    private NetworkMetaDataService networkMetaDataService;

    @Test
    void loadMetaData() {
        when(graphHopperNetworkSettingsBuilder.getMetaDataPath()).thenReturn(Path.of(EXPECT_FILE_PATH_STRING));
        assertThat(networkMetaDataService.loadMetaData()).isEqualTo(new GraphhopperMetaData(20241231));
    }

    @Test
    void loadMetaData_fail_ioexception() {
        when(graphHopperNetworkSettingsBuilder.getMetaDataPath()).thenReturn(Path.of(FILE_DOES_NOT_EXIST));

        assertThat(catchThrowable(() -> networkMetaDataService.loadMetaData()))
                .withFailMessage("Could not load meta-data from file path: file-does-not-exist")
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @SneakyThrows
    void saveMetaData() {
        Path tempFile = Files.createTempFile("meta-data", ".json");
        when(graphHopperNetworkSettingsBuilder.getMetaDataPath()).thenReturn(tempFile);

        networkMetaDataService.saveMetaData(new GraphhopperMetaData(20241231));
        assertThat(Files.exists(tempFile)).isTrue();
        FileUtils.contentEquals(EXPECTED_CONTENT, tempFile.toFile());
    }

    @Test
    @SneakyThrows
    void saveMetaData_fail_ioexception() {

        Path tempDirectory = Files.createTempDirectory(TMP_DIRECTORY_IS_NOT_A_WRITEABLE_FILE);
        when(graphHopperNetworkSettingsBuilder.getMetaDataPath())
                .thenReturn(tempDirectory);

        assertThat(catchThrowable(() -> networkMetaDataService.saveMetaData(new GraphhopperMetaData(20241231))))
                .withFailMessage("Could not write meta-data to file path: " + tempDirectory)
                .isInstanceOf(IllegalStateException.class);
    }
}