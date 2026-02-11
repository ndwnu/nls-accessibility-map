package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.writers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.configuration.GenerateConfiguration;
import org.springframework.stereotype.Component;

@Component
public class GeoJsonObjectMapperFactory {

    public ObjectMapper create(GenerateConfiguration generateConfiguration) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        if (generateConfiguration.prettyPrintJson()) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        return objectMapper;
    }
}
