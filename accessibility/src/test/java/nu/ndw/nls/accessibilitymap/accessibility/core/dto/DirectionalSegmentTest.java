package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import org.junit.jupiter.api.Test;

class DirectionalSegmentTest {

    @Test
    void getRoadSectionId() {

        RoadSection roadSection = RoadSection.builder()
                .id(123L)
                .build();

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .roadSection(roadSection)
                .build();

        DirectionalSegment directionalSegment = DirectionalSegment.builder()
                .roadSectionFragment(roadSectionFragment)
                .build();

        assertThat(directionalSegment.getRoadSectionId()).isEqualTo(roadSection.getId());
    }

    @Test
    void hasRestrictions() {

        DirectionalSegment directionalSegment = DirectionalSegment.builder()
                .restrictions(List.of(mock(Restriction.class)))
                .build();

        assertThat(directionalSegment.hasRestrictions()).isTrue();
    }

    @Test
    void hasTrafficSign_noTrafficSigns() {

        DirectionalSegment directionalSegment = DirectionalSegment.builder().build();

        assertThat(directionalSegment.hasRestrictions()).isFalse();
    }
}
