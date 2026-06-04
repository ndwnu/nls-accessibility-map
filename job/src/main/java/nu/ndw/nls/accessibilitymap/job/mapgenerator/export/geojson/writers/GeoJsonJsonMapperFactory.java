package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.writers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.configuration.GenerateConfiguration;
import org.springframework.stereotype.Component;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.json.JsonMapper.Builder;

@Component
public class GeoJsonJsonMapperFactory {

    public JsonMapper create(GenerateConfiguration generateConfiguration) {

        Builder jsonMapperBuilder = JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(Include.NON_NULL).withValueInclusion(Include.NON_NULL));


        if (generateConfiguration.prettyPrintJson()) {
            jsonMapperBuilder.enable(SerializationFeature.INDENT_OUTPUT);
        }

        return jsonMapperBuilder.build();
    }
}
