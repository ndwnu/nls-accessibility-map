package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.With;
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

        @Min(1) double searchRadiusInMeters,

        /*
         * Is mutually exclusive with:
         *  - addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections
         *  - addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections
         *  - writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible
         */
        @NotNull Boolean addAllRoadSectionFragments,

        @NotNull Boolean addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections,

        @NotNull Boolean addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections,

        @NotNull Boolean writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible,

        @NotNull Boolean addTrafficSignsAsPoints,

        @NotNull Boolean addTrafficSignsAsLineStrings,

        @NotNull @Min(1) Integer trafficSignLineStringDistanceInMeters,

        boolean prettyPrintJson) {

    public Path getGenerationDirectoryPath(OffsetDateTime startTime) {

        return rootExportDirectory.resolve(
                DateTimeFormatter.ofPattern(relativeExportDirectoryPattern)
                        .format(startTime.atZoneSameInstant(zone)));
    }
}
