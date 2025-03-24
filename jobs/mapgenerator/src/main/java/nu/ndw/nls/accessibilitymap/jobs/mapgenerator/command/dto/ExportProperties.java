package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record ExportProperties(
        @NotNull String name,
        @NotNull @NotEmpty Set<ExportType> exportTypes,
        @NotNull Integer nwbVersion,
        @NotNull Boolean publishEvents,
        @NotNull OffsetDateTime startTime,
        @NotNull AccessibilityRequest accessibilityRequest,
        @NotNull Boolean includeOnlyTimeWindowedSigns,
        @NotNull GenerateConfiguration generateConfiguration,
        @Positive double polygonMaxDistanceBetweenPoints) {

}
