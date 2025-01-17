package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record RoadSectionMetaData(
        LocalDate validFrom,
        String name,
        String nameSource,
        String townName,
        String municipalityName) {

}
