package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record AnalyseProperties(
        @NotNull String name,
        @NotNull Integer nwbVersion,
        @NotNull Boolean reportIssues,
        @NotNull OffsetDateTime startTime,
        @NotNull AccessibilityRequest accessibilityRequest) {

}
