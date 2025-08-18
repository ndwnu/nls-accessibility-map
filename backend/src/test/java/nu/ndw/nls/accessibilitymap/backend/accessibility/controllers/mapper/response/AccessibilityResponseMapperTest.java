package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MatchedRoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityResponseMapperTest {

    private static final long ROAD_SECTION_ID = 123L;

    private AccessibilityResponseMapper accessibilityResponseMapper;

    @Mock
    private AccessibilityReasonsJsonMapper accessibilityReasonsJsonMapper;

    @Mock
    private Accessibility accessibility;

    @Mock
    private RoadSection roadSection;

    @BeforeEach
    void setup() {

        accessibilityResponseMapper = new AccessibilityResponseMapper(accessibilityReasonsJsonMapper);
    }

    @Test
    void map() {

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(accessibility.toRoadSection()).thenReturn(roadSection);
        when(accessibility.reasons()).thenReturn(Collections.emptyList());
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        when(roadSection.isBackwardAccessible()).thenReturn(true);

        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility);

        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .backwardAccessible(true)
                .forwardAccessible(true)
                .build();

        final MatchedRoadSectionJson expectedMatchedRoadSectionJson = MatchedRoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .backwardAccessible(true)
                .forwardAccessible(true)
                .reasons(List.of())
                .build();
        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .matchedRoadSection(expectedMatchedRoadSectionJson)
                .build());

    }

    @Test
    void map_nullAccessibilityForward() {

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(accessibility.toRoadSection()).thenReturn(roadSection);
        when(accessibility.reasons()).thenReturn(Collections.emptyList());
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        when(roadSection.hasForwardSegments()).thenReturn(false);
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.isBackwardAccessible()).thenReturn(true);

        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility);

        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .backwardAccessible(true)
                .build();
        final MatchedRoadSectionJson expectedMatchedRoadSectionJson = MatchedRoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .backwardAccessible(true)
                .reasons(List.of())
                .build();

        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .matchedRoadSection(expectedMatchedRoadSectionJson)
                .build());
    }

    @Test
    void map_nullAccessibilityBackward() {

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(accessibility.toRoadSection()).thenReturn(roadSection);
        when(accessibility.reasons()).thenReturn(Collections.emptyList());
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(false);

        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility);

        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .forwardAccessible(true)
                .build();
        final MatchedRoadSectionJson expectedMatchedRoadSectionJson = MatchedRoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .forwardAccessible(true)
                .reasons(List.of())
                .build();

        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .matchedRoadSection(expectedMatchedRoadSectionJson)
                .build());
    }
}
