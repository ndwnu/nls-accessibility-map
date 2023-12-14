package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityResponseMapper {

    private final RoadSectionJsonResponseMapper roadSectionJsonResponseMapper;

    public AccessibilityMapResponseJson map(SortedMap<Integer, RoadSection> idToRoadSectionMap,
            Integer requestedRoadSectionId) {
        List<RoadSectionJson> list = idToRoadSectionMap.values().stream()
                // Only keep road sections affected by restrictions
                .filter(this::isRestrictedInAnyDirection)
                .map(roadSectionJsonResponseMapper::mapToRoadSection)
                .toList();

        return new AccessibilityMapResponseJson()
                .inaccessibleRoadSections(list)
                .requestedLocation(Optional.ofNullable(idToRoadSectionMap.get(requestedRoadSectionId))
                        .map(roadSectionJsonResponseMapper::mapToRoadSection)
                        .orElse(null));
    }

    private boolean isRestrictedInAnyDirection(RoadSection r) {
        return r.getForwardAccessible() == Boolean.FALSE || r.getBackwardAccessible() == Boolean.FALSE;
    }

}
