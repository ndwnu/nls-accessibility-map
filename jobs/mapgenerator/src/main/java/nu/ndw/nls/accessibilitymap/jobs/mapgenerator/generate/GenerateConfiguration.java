package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GenerateProperties.class)
public class GenerateConfiguration {

    private final GenerateProperties generateProperties;
    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeoJsonProperties getConfiguration(GenerateGeoJsonType generateGeoJsonType) {
        return generateProperties.getGeojson().get(generateGeoJsonType);
    }

    public Point getStartLocation() {
        return geometryFactoryWgs84.createPoint(new Coordinate( generateProperties.getStartLocationLongitude(),
                                                                generateProperties.getStartLocationLatitude()));
    }
}
