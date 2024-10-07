package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.BlobStorageLocationMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.writers.FileService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    private static final LocalDate VERSION_DATE = LocalDate.of(2024, 12, 31);
    private static final String TESTCONTENT = "testcontent";
    private static final String DIFFERENTCONTENT = "differentcontent";
    @Mock
    private BlobStorageLocationMapper blobStorageLocationMapper;

    @InjectMocks
    private FileService fileService;

    @Test
    @SneakyThrows
    void uploadFile_ok_newFile() {
        Path generatedFile = Files.createTempFile("generate", ".json");
        Files.writeString(generatedFile, TESTCONTENT);

        Path tempRootDirectory = Files.createTempDirectory("fileservice");
        Path tempDirectory = tempRootDirectory.resolve("v1/windowTimes");
        Path destinationFile = tempDirectory.resolve("result.json");

        when(blobStorageLocationMapper.map(TrafficSignType.C6, VERSION_DATE)).thenReturn(destinationFile);
        fileService.uploadFile(TrafficSignType.C6, generatedFile, VERSION_DATE);

        assertTrue(Files.exists(destinationFile));
        assertEquals(TESTCONTENT, Files.readString(destinationFile));
    }

    @Test
    @SneakyThrows
    void uploadFile_ok_existingFile() {
        Path generatedFile = Files.createTempFile("generate", ".json");
        Files.writeString(generatedFile, TESTCONTENT);

        Path tempRootDirectory = Files.createTempDirectory("fileservice");
        Path tempDirectory = tempRootDirectory.resolve("v1/windowTimes");
        Path destinationFile = tempDirectory.resolve("result.json");

        // Create a file at the destination with different content
        Files.createDirectories(tempDirectory);
        Files.writeString(destinationFile, DIFFERENTCONTENT);

        when(blobStorageLocationMapper.map(TrafficSignType.C6, VERSION_DATE)).thenReturn(destinationFile);
        fileService.uploadFile(TrafficSignType.C6, generatedFile, VERSION_DATE);

        assertTrue(Files.exists(destinationFile));
        assertEquals(TESTCONTENT, Files.readString(destinationFile));
    }

    @Test
    @SneakyThrows
    void createTempGeoJsonFile_ok() {
        Path tempFile = fileService.createTmpGeoJsonFile(GeoGenerationProperties.builder()
                .trafficSignType(TrafficSignType.C6)
                .build());

        assertTrue(tempFile.getFileName().toString().startsWith("accessibility-c6-"));
        assertTrue(tempFile.getFileName().toString().endsWith(".geojson"));

        assertTrue(Files.exists(tempFile));

        assertEquals(Set.of(OWNER_READ, OWNER_WRITE, OTHERS_READ), Files.getPosixFilePermissions(tempFile));
    }

}