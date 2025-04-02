package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    private FileService fileService;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        fileService = new FileService();
    }

    @Test
    void createTmpFile() throws IOException {

        Path tempFile = fileService.createTmpFile("file", ".tmp");

        assertTrue(tempFile.getFileName().toString().startsWith("file"));
        assertTrue(tempFile.getFileName().toString().endsWith(".tmp"));

        assertTrue(Files.exists(tempFile));

        assertEquals(Set.of(OWNER_READ, OWNER_WRITE, OTHERS_READ), Files.getPosixFilePermissions(tempFile));
    }

    @Test
    void createTmpFile_exception() {

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            IOException exception = mock(IOException.class);
            files.when(() -> Files.createTempFile(eq("file"), eq(".tmp"), any())).thenThrow(exception);

            assertThat(catchThrowable(() -> fileService.createTmpFile("file", ".tmp")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Failed to create tmp file.")
                    .hasCause(exception);
        }
    }

    @Test
    void moveFileAndOverride() throws IOException {

        Path source = Files.createTempFile("file1", ".tmp",
                PosixFilePermissions.asFileAttribute(Set.of(OWNER_READ, OWNER_WRITE, OTHERS_READ)));
        Path destinationDir = Files.createTempDirectory("dir");
        Path destination = Files.createTempFile(destinationDir, "file2", ".tmp");
        Files.delete(destination);
        Files.delete(destinationDir);

        try {
            Files.writeString(source, "abc");

            fileService.moveFileAndOverride(source, destination);

            assertThat(Files.readString(destination)).isEqualTo("abc");
            assertEquals(Set.of(OWNER_READ, OWNER_WRITE, OTHERS_READ), Files.getPosixFilePermissions(destination));

            assertThat(Files.exists(destinationDir)).isTrue();
            assertEquals(Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_EXECUTE),
                    Files.getPosixFilePermissions(destinationDir));
        } finally {
            deleteFile(source);
            deleteFile(destination);
            deleteFile(destinationDir);
        }
    }

    @Test
    void moveFileAndOverride_destinationExists() throws IOException {

        Path source = Files.createTempFile("file1", ".tmp");
        Path destination = Files.createTempFile("file2", ".tmp");

        try {
            Files.writeString(source, "abc");

            fileService.moveFileAndOverride(source, destination);

            assertThat(Files.readString(destination)).isEqualTo("abc");
        } finally {
            loggerExtension.containsLog(Level.WARN, "Overwriting existing file %s".formatted(destination));

            deleteFile(source);
            deleteFile(destination);
        }
    }

    @Test
    void moveFileAndOverride_exception() throws IOException {

        Path source = Files.createTempFile("file1", ".tmp");
        Path destination = Files.createTempFile("file2", ".tmp");

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            IOException exception = mock(IOException.class);
            files.when(() -> Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING)).thenThrow(exception);

            assertThat(catchThrowable(() -> fileService.moveFileAndOverride(source, destination)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Error moving file from %s to %s.".formatted(source, destination))
                    .hasCause(exception);
        }
    }

    private void deleteFile(Path source) throws IOException {
        if (source.toFile().exists()) {
            Files.delete(source);
        }
    }
}
