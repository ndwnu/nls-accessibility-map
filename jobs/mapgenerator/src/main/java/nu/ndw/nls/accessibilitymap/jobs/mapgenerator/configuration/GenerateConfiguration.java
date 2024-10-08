package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSignType;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@EnableConfigurationProperties(GenerateProperties.class)
public class GenerateConfiguration {

    private final GenerateProperties generateProperties;
    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    private final ObjectMapper objectMapper;

    public GenerateConfiguration(GenerateProperties generateProperties, GeometryFactoryWgs84 geometryFactoryWgs84) {
        this.generateProperties = generateProperties;
        this.geometryFactoryWgs84 = geometryFactoryWgs84;

        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        if (generateProperties.isPrettyPrintJson()) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
    }

    public GeoJsonProperties getConfiguration(TrafficSignType trafficSignType) {
        return generateProperties.getGeoJsonProperties().get(trafficSignType);
    }
}
