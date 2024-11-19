package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileService {

    private static final FileAttribute<?> FILE_PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(OWNER_READ, OWNER_WRITE, OTHERS_READ));

    private static final FileAttribute<?> FOLDER_PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));

    public Path createTmpFile(String fileName, String fileExtension) {

        try {
            return Files.createTempFile(fileName, fileExtension, FILE_PERMISSIONS);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create tmp file.", e);
        }
    }

    public void moveFileAndOverride(Path tempFile, Path exportFile) {

        try {
            if (Files.exists(exportFile)) {
                log.warn("Overwriting existing file {}", exportFile);
            } else {
                Files.createDirectories(exportFile.getParent(), FOLDER_PERMISSIONS);
            }

            Files.move(tempFile, exportFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Error moving file from %s to %s.".formatted(tempFile, exportFile),
                    exception);
        }
    }
}
