package nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record AnalyseAsymmetricTrafficSignsConfiguration(
        @NotNull String name,
        @NotNull Integer nwbVersion,
        @NotNull Boolean reportIssues,
        @NotNull OffsetDateTime startTime,
        @NotNull AccessibilityRequest accessibilityRequest) {

}
