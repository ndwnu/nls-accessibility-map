package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record RoadSectionMetaData(

        int versionId,
        LocalDate validFrom,
        String name,
        String nameSource,
        String townName,
        String municipalityName
) {

}
