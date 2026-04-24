package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record NwbVersion(int versionId, LocalDate versionDate) {

}
