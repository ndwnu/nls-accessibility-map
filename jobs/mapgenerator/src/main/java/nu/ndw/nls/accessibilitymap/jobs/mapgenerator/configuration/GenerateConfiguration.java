package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.jobs.generate")
@Validated
public record GenerateConfiguration(

        @NotNull ZoneId zone,
        @NotNull Path rootExportDirectory,
        @NotBlank String relativeExportDirectoryPattern,
        @Min(50) @Max(54) double startLocationLatitude,
        @Min(3) @Max(8) double startLocationLongitude,
        @Min(1) double searchRadiusInMeters,
        @NotNull Boolean addAllRoadSectionFragments,
        @NotNull Boolean addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections,
        @NotNull Boolean addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections,
        @NotNull Boolean writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible,
        @NotNull Boolean addTrafficSignsAsPoints,
        @NotNull Boolean addTrafficSignsAsLineStrings,
        boolean prettyPrintJson
) {

    public Path getGenerationDirectionPath(GeoGenerationProperties geoGenerationProperties) {
        return rootExportDirectory.resolve(
                DateTimeFormatter.ofPattern(relativeExportDirectoryPattern)
                        .format(geoGenerationProperties.startTime().atZoneSameInstant(zone)));
    }
}
