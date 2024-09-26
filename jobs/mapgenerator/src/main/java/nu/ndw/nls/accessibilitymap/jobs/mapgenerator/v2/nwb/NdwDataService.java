package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.nwb;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.mappers.RoadSectionMetaDataMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSectionWithDirection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NdwDataService {

    private final NwbRoadSectionCrudService nwbRoadSectionCrudService;

    private final RoadSectionMetaDataMapper roadSectionMetaDataMapper;

    public void addNdwDataToRoadSections(int nwbVersion, List<RoadSectionWithDirection> roadSectionWithDirections) {

        roadSectionWithDirections.forEach(roadSectionWithDirection ->
                addNdwDataToRoadSectionWithDirection(nwbVersion, roadSectionWithDirection));
    }

    private void addNdwDataToRoadSectionWithDirection(
            int nwbVersion,
            RoadSectionWithDirection roadSectionWithDirection) {

        NwbRoadSectionDto nwbRoadSection = nwbRoadSectionCrudService
                .findById(new Id(nwbVersion, roadSectionWithDirection.getRoadSectionId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Failed to find road section with id '%s' and by version '%s'".formatted(
                                roadSectionWithDirection.getRoadSectionId(),
                                nwbVersion)));

        roadSectionWithDirection.setMetaData(roadSectionMetaDataMapper.map(nwbRoadSection));
    }
}
