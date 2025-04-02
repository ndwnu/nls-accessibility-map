package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityResponseV2Mapper {

    private final RoadSectionJsonResponseMapper roadSectionJsonResponseMapper;

    public AccessibilityMapResponseJson map(Accessibility accessibility,
            Integer requestedRoadSectionId) {

        List<RoadSectionJson> inaccessibleRoadSections = accessibility.combinedAccessibility()
                .stream()
                .filter(RoadSection::isRestrictedInAnyDirection)
                .map(AccessibilityResponseV2Mapper::mapToRoadSection)
                .toList();

        return new AccessibilityMapResponseJson()
                .inaccessibleRoadSections(inaccessibleRoadSections)
                .matchedRoadSection(Optional.ofNullable(requestedRoadSectionId)
                        .flatMap(accessibility::findById)
                        .map(AccessibilityResponseV2Mapper::mapToRoadSection)
                        .orElse(null));
    }

    private static RoadSectionJson mapToRoadSection(RoadSection roadSection) {
        return RoadSectionJson
                .builder()
                .roadSectionId(Math.toIntExact(roadSection.getId()))
                .forwardAccessible(roadSection.isForwardAccessible())
                .backwardAccessible(roadSection.isBackwardAccessible())
                .build();
    }


}
