package nu.ndw.nls.accessibilitymap.accessibility.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class JsonWriter {

    private final JsonMapper jsonMapper;

    public <T> void writeJsonToFile(Path target, String fileName, T data) throws IOException {

        Path jsonFilePath = target.resolve(fileName);
        if (!Files.exists(jsonFilePath)) {
            Files.createDirectories(target);
            Files.createFile(jsonFilePath);
        }
        try (JsonGenerator jsonGenerator = jsonMapper.createGenerator(Files.newOutputStream(jsonFilePath))) {

            jsonMapper.writeValue(jsonGenerator, data);
        }
    }
}
