package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import lombok.Builder;
import org.springframework.validation.annotation.Validated;

@Builder
@Validated
public record BlockedRoadSection(
        int roadSectionId,
        boolean forwardAccessible,
        boolean backwardAccessible) {

}