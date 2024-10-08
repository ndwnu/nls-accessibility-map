package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.Setter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.jobs.generate")
public class GenerateConfiguration {

    @NotNull
    private ZoneId zone;

    @NotNull
    private Path rootExportDirectory;

    @NotBlank
    private String relativeExportDirectoryPattern;

    @Min(50)
    @Max(54)
    private double startLocationLatitude;

    @Min(3)
    @Max(8)
    private double startLocationLongitude;

    @Min(1)
    private double searchRadiusInMeters;

    private boolean prettyPrintJson;

    public Path getGenerationDirectionPath(GeoGenerationProperties geoGenerationProperties) {

        return rootExportDirectory.resolve(
                DateTimeFormatter.ofPattern(relativeExportDirectoryPattern)
                        .format(geoGenerationProperties.startTime().atZoneSameInstant(zone)));
    }
}
