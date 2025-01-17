package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.nwb.services;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.nwb.mappers.RoadSectionMetaDataMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NdwDataService {

    private final NwbRoadSectionCrudService nwbRoadSectionCrudService;

    private final RoadSectionMetaDataMapper roadSectionMetaDataMapper;

    public void addNwbDataToAccessibility(Accessibility accessibility, int nwbVersion) {

        accessibility.combinedAccessibility().forEach(roadSection ->
                addNdwDataToRoadSection(roadSection, nwbVersion));

        accessibility.accessibleRoadSectionsWithAppliedRestrictions().forEach(roadSection ->
                addNdwDataToRoadSection(roadSection, nwbVersion));

        accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions().forEach(roadSection ->
                addNdwDataToRoadSection(roadSection, nwbVersion));
    }

    private void addNdwDataToRoadSection(
            RoadSection roadSection,
            int nwbVersion) {

        NwbRoadSectionDto nwbRoadSection = nwbRoadSectionCrudService
                .findById(new Id(nwbVersion, roadSection.getId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Failed to find road section with id '%s' and by version '%s'".formatted(
                                roadSection.getId(),
                                nwbVersion)));

        roadSection.setMetaData(roadSectionMetaDataMapper.map(nwbRoadSection));
    }
}
