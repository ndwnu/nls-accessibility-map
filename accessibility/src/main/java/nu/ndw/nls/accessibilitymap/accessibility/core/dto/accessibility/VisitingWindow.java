package nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility;

import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record VisitingWindow(OffsetDateTime start, OffsetDateTime end) {

}
