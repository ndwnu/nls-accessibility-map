package nu.ndw.nls.accessibilitymap.accessibility.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonWriterTest {

    private Path testDir;

    private JsonWriter jsonWriter;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        testDir = Files.createTempDirectory(this.getClass().getSimpleName());
        jsonWriter = new JsonWriter(new ObjectMapper());
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        FileUtils.deleteDirectory(testDir.toFile());
    }

    @SneakyThrows
    @Test
    void writeJsonToFile_files_exist() {
        Data data = new Data("test");
        String jsonFileName = "test.json";
        Files.createDirectories(testDir);
        Files.createFile(testDir.resolve(jsonFileName));

        jsonWriter.writeJsonToFile(testDir.toAbsolutePath(), jsonFileName, data);

        File writtenFile = new File(testDir.toFile(), jsonFileName);
        String writtenJson = FileUtils.readFileToString(writtenFile, StandardCharsets.UTF_8);
        String expectedJson = """
                {key:"test"}
                """;
        assertThatJson(writtenJson).isEqualTo(expectedJson);
    }

    @SneakyThrows
    @Test
    void writeJsonToFile() {
        Data data = new Data("test");

        jsonWriter.writeJsonToFile(testDir.toAbsolutePath(), "test.json", data);

        File writtenFile = new File(testDir.toAbsolutePath().toFile(), "test.json");
        String writtenJson = FileUtils.readFileToString(writtenFile, StandardCharsets.UTF_8);
        String expectedJson = """
                {key:"test"}
                """;
        assertThatJson(writtenJson).isEqualTo(expectedJson);
    }

    @Value
    private static class Data {

        String key;
    }
}
