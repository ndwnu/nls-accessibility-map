package nu.ndw.nls.accessibilitymap.accessibility.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.mappers.AccessibleRoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRoadSection;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.shared.nwb.services.NwbRoadSectionService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessibleRoadSectionsService {

    private final NwbRoadSectionService nwbRoadSectionService;
    private final AccessibleRoadSectionMapper accessibleRoadSectionMapper;
    private final AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData;

    /**
     * The context exists around a single NWB map version, therefor it is acceptable to use a map to cache the
     * sub-results
     */
    private final Map<Integer, List<AccessibilityRoadSection>> municipalityIdToRoadSections = new HashMap<>();

    @Transactional
    public List<AccessibilityRoadSection> getRoadSectionsByMunicipalityId(int municipalityId) {
        return municipalityIdToRoadSections.computeIfAbsent(municipalityId, this::createRoadSectionMap);
    }

    private List<AccessibilityRoadSection> createRoadSectionMap(int municipalityId) {
        try (Stream<NwbRoadSectionDto> roadSections =
                nwbRoadSectionService.findLazyCar(accessibilityGraphhopperMetaData.nwbVersion(),
                        Collections.singleton(municipalityId))) {

            return roadSections.map(accessibleRoadSectionMapper::map).toList();
        }
    }

    @Transactional
    public List<AccessibilityRoadSection> getRoadSections() {
        return nwbRoadSectionService.findLazyCar(accessibilityGraphhopperMetaData.nwbVersion(), null)
                .map(accessibleRoadSectionMapper::map)
                .toList();
    }

}
