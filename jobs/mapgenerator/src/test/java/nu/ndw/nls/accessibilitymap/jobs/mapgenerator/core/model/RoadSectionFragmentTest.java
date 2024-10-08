package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
    void getSegments() {

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegments(List.of(forwardSegment))
                .backwardSegments(List.of(backwardSegment))
                .build();

        assertThat(roadSectionFragment.getSegments()).containsExactly(forwardSegment, backwardSegment);
    }
}