package nu.ndw.nls.accessibilitymap.accessibility.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import org.junit.jupiter.api.Test;

class JsonNwbDataStreamReaderWriterTest {

    @SneakyThrows
    @Test
    void readJsonData() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNwbDataStreamReaderWriter reader = new JsonNwbDataStreamReaderWriter(objectMapper);
        Path path = Paths.get(
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResource("test-nwb-data.json")
                ).toURI()
        );
        NwbData nwbData = reader.readJsonData(path);
    }
}
