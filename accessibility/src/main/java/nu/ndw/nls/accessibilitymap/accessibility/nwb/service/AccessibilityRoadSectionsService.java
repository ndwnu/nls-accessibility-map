package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.mappers.AccessibilityNwbRoadSectionMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessibilityRoadSectionsService {

    private final NwbRoadSectionService nwbRoadSectionService;

    private final AccessibilityNwbRoadSectionMapper accessibleRoadSectionMapper;

    private final GraphhopperMetaData graphhopperMetaData;

    /**
     * The context exists around a single NWB map version, therefor it is acceptable to use a map to cache the sub-results
     */
    private final Map<Integer, List<AccessibilityNwbRoadSection>> municipalityIdToRoadSections = new HashMap<>();

    @Transactional
    public List<AccessibilityNwbRoadSection> getRoadSectionsByMunicipalityId(int municipalityId) {
        return municipalityIdToRoadSections.computeIfAbsent(municipalityId, this::createRoadSectionMap);
    }

    private List<AccessibilityNwbRoadSection> createRoadSectionMap(int municipalityId) {
        try (Stream<NwbRoadSectionDto> roadSections =
                nwbRoadSectionService.findLazyCar(graphhopperMetaData.nwbVersion(), Collections.singleton(municipalityId))) {

            return roadSections.map(accessibleRoadSectionMapper::map).toList();
        }
    }

    @Transactional
    public List<AccessibilityNwbRoadSection> getRoadSections() {
        return nwbRoadSectionService.findLazyCar(graphhopperMetaData.nwbVersion(), null)
                .map(accessibleRoadSectionMapper::map)
                .toList();
    }
}
