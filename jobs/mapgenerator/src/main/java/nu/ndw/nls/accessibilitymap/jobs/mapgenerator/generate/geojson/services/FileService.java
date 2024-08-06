package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.BlobStorageLocationMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileService {

    private static final FileAttribute<?> FILE_PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(OWNER_READ, OWNER_WRITE, OTHERS_READ));

    private static final FileAttribute<?> FOLDER_PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));

    private final BlobStorageLocationMapper blobStorageLocationMapper;

    public void uploadFile(GenerateGeoJsonType type, Path geojsonTmpResult) {
        Path mapDestinationPath = blobStorageLocationMapper.map(type, LocalDate.now());

        Path mapDirectoryPath = mapDestinationPath.getParent();

        try {
            if (Files.exists(mapDestinationPath)) {
                log.warn("Overwriting existing file {}", mapDestinationPath);
                Files.delete(mapDestinationPath);
            } else {
                Files.createDirectories(mapDirectoryPath, FOLDER_PERMISSIONS);
            }

            Files.move(geojsonTmpResult, mapDestinationPath);

        } catch (IOException e) {
            throw new IllegalStateException("Error moving file from " + geojsonTmpResult + " to " + mapDestinationPath,
                    e);
        }
    }

}
