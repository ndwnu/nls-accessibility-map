package nu.ndw.nls.accessibilitymap.accessibility.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public NwbData roadJsonBData(Path jsonFile) {
        ObjectMapper mapper = new ObjectMapper(new SmileFactory());

        try (InputStream is = Files.newInputStream(jsonFile);
                JsonParser parser = mapper.getFactory().createParser(is)) {
            Integer versionId = null;
            List<AccessibilityNwbRoadSection> sections = new ArrayList<>();
            parser.nextToken(); // START_OBJECT
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                if ("nwbVersionId".equals(field)) {
                    parser.nextToken();
                    versionId = parser.getIntValue();
                }
                if ("accessibilityNwbRoadSections".equals(field)) {
                    parser.nextToken(); // START_ARRAY
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        AccessibilityNwbRoadSection section = mapper.readValue(parser, AccessibilityNwbRoadSection.class);
                        sections.add(section);
                    }
                }
            }
            return new NwbData(versionId, sections);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void writeJsonBData(NwbData nwbData, Path jsonFile) {
        try (OutputStream os = Files.newOutputStream(Path.of("roads.smile"))) {
            ObjectMapper mapper = new ObjectMapper(new SmileFactory());
            JsonGenerator gen = mapper.getFactory().createGenerator(os);
            gen.writeStartObject();
            gen.writeNumberField("nwbVersionId", nwbData.getNwbVersionId());
            gen.writeFieldName("accessibilityNwbRoadSections");
            gen.writeStartArray();
            for (AccessibilityNwbRoadSection section : nwbData.getAccessibilityNwbRoadSections()) {
                mapper.writeValue(gen, section);
            }
            gen.writeEndArray();
            gen.writeEndObject();
            gen.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
