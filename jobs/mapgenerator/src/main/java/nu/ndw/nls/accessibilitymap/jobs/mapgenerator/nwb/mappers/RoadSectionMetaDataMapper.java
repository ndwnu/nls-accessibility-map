package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.nwb.mappers;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionMetaData;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.springframework.stereotype.Component;

@Component
public class RoadSectionMetaDataMapper {

    public RoadSectionMetaData map(NwbRoadSectionDto nwbRoadSection) {

        return RoadSectionMetaData.builder()
                .validFrom(nwbRoadSection.getValidFrom())
                .municipalityName(nwbRoadSection.getMunicipalityName())
                .name(nwbRoadSection.getRoadName())
                .nameSource(nwbRoadSection.getRoadNameSource())
                .townName(nwbRoadSection.getTownName())
                .build();
    }
}
