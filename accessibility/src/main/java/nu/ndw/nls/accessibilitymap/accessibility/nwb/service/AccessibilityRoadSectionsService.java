package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
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

    /**
     * Key is a combination of nwbVersion and municipalityId, so we create a cache per nwb version.
     */
    private final Map<String, List<AccessibilityNwbRoadSection>> municipalityIdToRoadSections = new HashMap<>();

    @Transactional
    public List<AccessibilityNwbRoadSection> getRoadSectionsByMunicipalityId(int nwbVersion, int municipalityId) {
        return municipalityIdToRoadSections.computeIfAbsent(
                getCacheKey(nwbVersion, municipalityId),
                missedCacheKey -> createRoadSectionMap(nwbVersion, municipalityId));
    }

    private List<AccessibilityNwbRoadSection> createRoadSectionMap(int nwbVersion, int municipalityId) {
        try (Stream<NwbRoadSectionDto> roadSections =
                nwbRoadSectionService.findLazyCar(nwbVersion, Collections.singleton(municipalityId))) {

            return roadSections.map(accessibleRoadSectionMapper::map).toList();
        }
    }

    private String getCacheKey(int nwbVersion, int municipalityId) {
        return nwbVersion + "-" + municipalityId;
    }
}
