package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
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
    }

    public GeoJsonProperties getConfiguration(CmdGenerateGeoJsonType cmdGenerateGeoJsonType) {
        return generateProperties.getGeojson().get(cmdGenerateGeoJsonType);
    }

    public Point getStartLocation() {
        return geometryFactoryWgs84.createPoint(new Coordinate( generateProperties.getStartLocationLongitude(),
                                                                generateProperties.getStartLocationLatitude()));
    }
}
