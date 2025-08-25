package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MatchedRoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityResponseMapper {

    private final AccessibilityReasonsJsonMapper accessibilityReasonsJsonMapper;

    public AccessibilityMapResponseJson map(Accessibility accessibility) {

        List<RoadSectionJson> inaccessibleRoadSections = accessibility.combinedAccessibility().stream()
                .filter(RoadSection::isRestrictedInAnyDirection)
                .map(AccessibilityResponseMapper::mapToRoadSection)
                .toList();

        MatchedRoadSectionJson matchedRoadSection = accessibility.toRoadSection()
                .map(roadSection -> mapToMatchedRoadSection(roadSection, accessibility.reasons()))
                .orElse(null);

        return new AccessibilityMapResponseJson()
                .inaccessibleRoadSections(inaccessibleRoadSections)
                .matchedRoadSection(matchedRoadSection);
    }

    private static RoadSectionJson mapToRoadSection(RoadSection roadSection) {

        return RoadSectionJson
                .builder()
                .roadSectionId(Math.toIntExact(roadSection.getId()))
                .forwardAccessible(roadSection.hasForwardSegments() ? roadSection.isForwardAccessible() : null)
                .backwardAccessible(roadSection.hasBackwardSegments() ? roadSection.isBackwardAccessible() : null)
                .build();
    }

    private MatchedRoadSectionJson mapToMatchedRoadSection(RoadSection roadSection, List<List<AccessibilityReason>> reasons) {

        return MatchedRoadSectionJson
                .builder()
                .roadSectionId(Math.toIntExact(roadSection.getId()))
                .forwardAccessible(roadSection.hasForwardSegments() ? roadSection.isForwardAccessible() : null)
                .backwardAccessible(roadSection.hasBackwardSegments() ? roadSection.isBackwardAccessible() : null)
                .reasons(accessibilityReasonsJsonMapper.mapToReasonJson(reasons))
                .build();
    }
}
