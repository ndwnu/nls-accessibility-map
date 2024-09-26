package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.nwb.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.mappers.RoadSectionMetaDataMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NdwDataService {

    private final NwbRoadSectionCrudService nwbRoadSectionCrudService;

    private final RoadSectionMetaDataMapper roadSectionMetaDataMapper;

    public void addNdwDataToRoadSections(List<RoadSection> roadSections, int nwbVersion) {

        roadSections.forEach(roadSection ->
                addNdwDataToRoadSection(roadSection, nwbVersion));
    }

    private void addNdwDataToRoadSection(
            RoadSection roadSection,
            int nwbVersion) {

        NwbRoadSectionDto nwbRoadSection = nwbRoadSectionCrudService
                .findById(new Id(nwbVersion, roadSection.getRoadSectionId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Failed to find road section with id '%s' and by version '%s'".formatted(
                                roadSection.getRoadSectionId(),
                                nwbVersion)));

        roadSection.setMetaData(roadSectionMetaDataMapper.map(nwbRoadSection));
    }
}
