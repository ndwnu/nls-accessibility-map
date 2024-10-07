package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.trafficsign.TrafficSignType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlobStorageLocationMapper {

    private final GenerateConfiguration generateConfiguration;

    public Path map(TrafficSignType trafficSignType, LocalDate version) {
        GeoJsonProperties properties = generateConfiguration.getConfiguration(trafficSignType);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(properties.getPathDatePattern());

        return generateConfiguration.getGenerateProperties().getRootGenerationDestination()
                .resolve(dateTimeFormatter.format(version));
    }

}
