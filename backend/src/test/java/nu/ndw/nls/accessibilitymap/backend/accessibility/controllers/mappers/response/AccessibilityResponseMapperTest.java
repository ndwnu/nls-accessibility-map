package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityResponseMapperTest {

    private static final long ROAD_SECTION_ID = 123L;

    private AccessibilityResponseMapper accessibilityResponseMapper;

    @Mock
    private Accessibility accessibility;

    @Mock
    private RoadSection roadSection;

    @BeforeEach
    void setup() {
        accessibilityResponseMapper = new AccessibilityResponseMapper();
    }

    @Test
    void map() {
        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        when(roadSection.isBackwardAccessible()).thenReturn(true);
        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility, (int) ROAD_SECTION_ID);
        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .backwardAccessible(true)
                .forwardAccessible(true)
                .build();
        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .matchedRoadSection(expectedRoadSection)
                .build());

    }

    @Test
    void map_nullAccessibilityForward() {
        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        when(roadSection.hasForwardSegments()).thenReturn(false);
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.isBackwardAccessible()).thenReturn(true);
        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility, (int) ROAD_SECTION_ID);
        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .backwardAccessible(true)
                .build();
        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .matchedRoadSection(expectedRoadSection)
                .build());

    }

    @Test
    void map_nullAccessibilityBackward() {
        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(false);

        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility, (int) ROAD_SECTION_ID);
        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .forwardAccessible(true)
                .build();
        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .matchedRoadSection(expectedRoadSection)
                .build());

    }

    @ParameterizedTest
    @MethodSource("provideRoadSectionValues")
    void map_noMatchedRoadSection(Optional<Integer> roadSectionId) {
        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(true);
        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(false);
        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility,
                roadSectionId.isEmpty() ? null : roadSectionId.get());
        final RoadSectionJson expectedRoadSection = RoadSectionJson.builder()
                .roadSectionId((int) ROAD_SECTION_ID)
                .forwardAccessible(true)
                .build();
        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of(expectedRoadSection))
                .build());

    }

    @Test
    void map_emptyResult() {
        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.isRestrictedInAnyDirection()).thenReturn(false);
        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(accessibility, null);
        assertThat(result).isEqualTo(AccessibilityMapResponseJson
                .builder()
                .inaccessibleRoadSections(List.of())
                .build());

    }

    private static Stream<Arguments> provideRoadSectionValues() {
        return Stream.of(
                Arguments.of(Optional.of(456)),
                Arguments.of(Optional.empty())

        );
    }
}
