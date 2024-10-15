package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFragmentTest {

    @Mock
    private DirectionalSegment forwardSegment;

    @Mock
    private DirectionalSegment backwardSegment;

    @Test
    void setForwardSegment() {

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder().build();
        roadSectionFragment.setForwardSegment(forwardSegment);

        assertThat(roadSectionFragment.getForwardSegment()).isEqualTo(forwardSegment);
        assertThat(catchThrowable(() -> roadSectionFragment.setForwardSegment(forwardSegment)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("forwardSegment has already been assigned. "
                        + "There should be always only one forwardSegment per RoadSectionFragment.");
    }

    @Test
    void setBackwardSegment() {

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder().build();
        roadSectionFragment.setBackwardSegment(backwardSegment);

        assertThat(roadSectionFragment.getBackwardSegment()).isEqualTo(backwardSegment);
        assertThat(catchThrowable(() -> roadSectionFragment.setBackwardSegment(backwardSegment)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("backSegment has already been assigned. "
                        + "There should be always only one backSegment per RoadSectionFragment.");
    }

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

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true, true
            true, false, false
            false, true, false
            false, false, false
            """)
    void isAccessibleFromAllSegments(boolean forwardAccessible, boolean backwardAccessible, boolean result) {

        when(forwardSegment.isAccessible()).thenReturn(forwardAccessible);
        if (forwardAccessible) {
            when(backwardSegment.isAccessible()).thenReturn(backwardAccessible);
        }

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegment(forwardSegment)
                .backwardSegment(backwardSegment)
                .build();

        assertThat(roadSectionFragment.isAccessibleFromAllSegments()).isEqualTo(result);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true, false
            true, false, false
            false, true, false
            false, false, true
            """)
    void isNotAccessibleFromAllSegments(boolean forwardAccessible, boolean backwardAccessible, boolean result) {

        when(forwardSegment.isAccessible()).thenReturn(forwardAccessible);
        if (!forwardAccessible) {
            when(backwardSegment.isAccessible()).thenReturn(backwardAccessible);
        }

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegment(forwardSegment)
                .backwardSegment(backwardSegment)
                .build();

        assertThat(roadSectionFragment.isNotAccessibleFromAllSegments()).isEqualTo(result);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true, false
            true, false, true
            false, true, true
            false, false, false
            """)
    void isPartiallyAccessible(boolean forwardAccessible, boolean backwardAccessible, boolean result) {

        when(forwardSegment.isAccessible()).thenReturn(forwardAccessible);
        when(backwardSegment.isAccessible()).thenReturn(backwardAccessible);

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegment(forwardSegment)
                .backwardSegment(backwardSegment)
                .build();

        assertThat(roadSectionFragment.isPartiallyAccessible()).isEqualTo(result);
    }
}