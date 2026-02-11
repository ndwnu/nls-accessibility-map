package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.MatchedRoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @Mock
    private List<List<AccessibilityReason>> reasons;

    @Mock
    private List<List<ReasonJson>> reasonsJson;

    @BeforeEach
    void setup() {

        accessibilityResponseMapper = new AccessibilityResponseMapper(accessibilityReasonsJsonMapper);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            false, false,
            true, true,
            true, false,
            false, true,
            """)
    void map(boolean isForwardAccessible, boolean isBackwardAccessible) {

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(accessibility.toRoadSection()).thenReturn(Optional.of(roadSection));
        when(accessibility.reasons()).thenReturn(reasons);
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(isForwardAccessible);
        when(roadSection.isBackwardAccessible()).thenReturn(isBackwardAccessible);
        when(accessibilityReasonsJsonMapper.mapToReasonJson(reasons)).thenReturn(reasonsJson);

        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility);

        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .forwardAccessible(isForwardAccessible)
                .backwardAccessible(isBackwardAccessible)
                .build();

        final MatchedRoadSectionJson expectedMatchedRoadSectionJson = MatchedRoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .forwardAccessible(isForwardAccessible)
                .backwardAccessible(isBackwardAccessible)
                .reasons(reasonsJson)
                .build();

        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .matchedRoadSection(expectedMatchedRoadSectionJson)
                .build());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            false, false,
            true, true,
            true, false,
            false, true,
            """)
    void map_missingDirectionalSegments(boolean forwardDirectionalSegmentMissing, boolean backwardDirectionalSegmentMissing) {

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(accessibility.toRoadSection()).thenReturn(Optional.of(roadSection));
        when(accessibility.reasons()).thenReturn(reasons);
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        if (!forwardDirectionalSegmentMissing) {
            when(roadSection.hasForwardSegments()).thenReturn(true);
            when(roadSection.isForwardAccessible()).thenReturn(true);
        }
        if (!backwardDirectionalSegmentMissing) {
            when(roadSection.hasBackwardSegments()).thenReturn(true);
            when(roadSection.isBackwardAccessible()).thenReturn(true);
        }
        when(accessibilityReasonsJsonMapper.mapToReasonJson(reasons)).thenReturn(reasonsJson);

        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility);

        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .forwardAccessible(forwardDirectionalSegmentMissing ? null : true)
                .backwardAccessible(backwardDirectionalSegmentMissing ? null : true)
                .build();

        final MatchedRoadSectionJson expectedMatchedRoadSectionJson = MatchedRoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .forwardAccessible(forwardDirectionalSegmentMissing ? null : true)
                .backwardAccessible(backwardDirectionalSegmentMissing ? null : true)
                .reasons(reasonsJson)
                .build();

        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .matchedRoadSection(expectedMatchedRoadSectionJson)
                .build());

    }
}
