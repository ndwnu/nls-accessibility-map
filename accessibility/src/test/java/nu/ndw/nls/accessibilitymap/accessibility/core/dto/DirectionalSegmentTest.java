package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionalSegmentTest {

    @Mock
    private TrafficSign trafficSign;

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
    void hasTrafficSigns() {

        DirectionalSegment directionalSegment = DirectionalSegment.builder()
                .trafficSigns(List.of(trafficSign))
                .build();

        assertThat(directionalSegment.hasTrafficSigns()).isTrue();
    }

    @Test
    void hasTrafficSign_noTrafficSigns() {

        DirectionalSegment directionalSegment = DirectionalSegment.builder().build();

        assertThat(directionalSegment.hasTrafficSigns()).isFalse();
    }
}
