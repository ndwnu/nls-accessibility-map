package nu.ndw.nls.accessibilitymap.accessibility.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonNwbDataStreamReaderWriter {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public NwbData readJsonData(Path jsonFile) {
        List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections = new ArrayList<>();
        Integer versionId = null;
        try (InputStream is = Files.newInputStream(jsonFile); JsonParser parser = objectMapper.getFactory().createParser(is)) {
            parser.nextToken();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                if ("nwbVersionId".equals(fieldName)) {
                    parser.nextToken();
                    versionId = parser.getIntValue();
                }
                if ("accessibilityNwbRoadSections".equals(fieldName)) {
                    parser.nextToken();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        AccessibilityNwbRoadSection section = objectMapper.readValue(parser, AccessibilityNwbRoadSection.class);
                        accessibilityNwbRoadSections.add(section);
                    }
                }
            }
        }
        return new NwbData(versionId, accessibilityNwbRoadSections);
    }
}
