package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFragmentTest {

    @Mock
    private DirectionalSegment forwardSegment;

    @Mock
    private DirectionalSegment backwardSegment;

    @Test
    void getSegments_ok() {

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegment(forwardSegment)
                .backwardSegment(backwardSegment)
                .build();

        assertThat(roadSectionFragment.getSegments()).containsExactly(forwardSegment, backwardSegment);
    }

    @Test
    void getSegments_ok_noBackSegment() {

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegment(forwardSegment)
                .build();

        assertThat(roadSectionFragment.getSegments()).containsExactly(forwardSegment);
    }
}