package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityResponseMapper {

    public AccessibilityMapResponseJson map(Accessibility accessibility, Integer requestedRoadSectionId) {

        List<RoadSectionJson> inaccessibleRoadSections = accessibility.combinedAccessibility().stream()
                .filter(RoadSection::isRestrictedInAnyDirection)
                .map(AccessibilityResponseMapper::mapToRoadSection)
                .toList();

        RoadSectionJson matchedRoadSection = Optional.ofNullable(requestedRoadSectionId)
                .flatMap(roadSectionId -> findRoadSectionById(accessibility.combinedAccessibility(), (long) roadSectionId))
                .map(AccessibilityResponseMapper::mapToRoadSection)
                .orElse(null);

        return new AccessibilityMapResponseJson()
                .inaccessibleRoadSections(inaccessibleRoadSections)
                .matchedRoadSection(matchedRoadSection);
    }

    private Optional<RoadSection> findRoadSectionById(Collection<RoadSection> roadSections, Long roadSectionId) {
        return roadSections.stream()
                .filter(roadSection -> roadSection.getId().equals(roadSectionId))
                .findFirst();
    }

    private static RoadSectionJson mapToRoadSection(RoadSection roadSection) {
        return RoadSectionJson
                .builder()
                .roadSectionId(Math.toIntExact(roadSection.getId()))
                .forwardAccessible(roadSection.hasForwardSegments() ? roadSection.isForwardAccessible() : null)
                .backwardAccessible(roadSection.hasBackwardSegments() ? roadSection.isBackwardAccessible() : null)
                .build();
    }

}
