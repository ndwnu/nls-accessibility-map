package nu.ndw.nls.accessibilitymap.accessibility.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonWriter {

    private final ObjectMapper objectMapper;

    public <T> void writeJsonToFile(Path target, @Nullable Path subFolder, String fileName, T data) throws IOException {

        Path destinationFolderPath;
        Path jsonFilePath;
        if (Objects.nonNull(subFolder)) {
            destinationFolderPath = target.resolve(subFolder);
        } else {
            destinationFolderPath = target;
        }
        jsonFilePath = destinationFolderPath.resolve(fileName);

        if (!Files.exists(jsonFilePath)) {
            Files.createDirectories(destinationFolderPath);
            Files.createFile(jsonFilePath);
        }
        try (JsonGenerator jsonGenerator = objectMapper
                .getFactory()
                .createGenerator(Files.newOutputStream(jsonFilePath))) {

            objectMapper.writeValue(jsonGenerator, data);
        }
    }
}
