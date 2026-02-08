package nu.ndw.nls.accessibilitymap.job.dataanalyser.command.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record AnalyseAsymmetricTrafficSignsConfiguration(
        @NotNull String name,
        @NotNull Boolean reportIssues,
        @NotNull OffsetDateTime startTime,
        @NotNull AccessibilityRequest accessibilityRequest) {

}
