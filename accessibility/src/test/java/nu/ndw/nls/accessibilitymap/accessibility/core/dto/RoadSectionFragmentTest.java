package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;
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
    void hasForwardSegment_noForwardSegment() {
        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder().build();
        assertThat(roadSectionFragment.hasForwardSegment()).isFalse();
    }

    @Test
    void hasForwardSegment_withForwardSegment() {
        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegment(forwardSegment)
                .build();
        assertThat(roadSectionFragment.hasForwardSegment()).isTrue();
    }


    @Test
    void hasBackwardSegment_noBackwardSegment() {
        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder().build();
        assertThat(roadSectionFragment.hasBackwardSegment()).isFalse();
    }

    @Test
    void hasBackwardSegment_withBackwardSegment() {
        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .backwardSegment(backwardSegment)
                .build();
        assertThat(roadSectionFragment.hasBackwardSegment()).isTrue();
    }

    @Test
    void isForwardAccessible_noForwardSegment() {
        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder().build();
        assertThat(roadSectionFragment.isForwardAccessible()).isFalse();
    }

    @Test
    void isForwardAccessible_withForwardSegment_notAccessible() {
        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegment(forwardSegment)
                .build();
        when(forwardSegment.isAccessible()).thenReturn(false);
        assertThat(roadSectionFragment.isForwardAccessible()).isFalse();
    }

    @Test
    void isBackwardAccessible_noBackwardSegment() {
        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder().build();
        assertThat(roadSectionFragment.isBackwardAccessible()).isFalse();
    }

    @Test
    void isBackwardAccessible_withBackwardSegment_notAccessible() {
        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .backwardSegment(backwardSegment)
                .build();
        when(backwardSegment.isAccessible()).thenReturn(false);
        assertThat(roadSectionFragment.isBackwardAccessible()).isFalse();
    }

    @Test
    void getSegments() {

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .forwardSegment(forwardSegment)
                .backwardSegment(backwardSegment)
                .build();

        assertThat(roadSectionFragment.getSegments()).containsExactly(forwardSegment, backwardSegment);
    }

    @Test
    void getSegments_noBackSegment() {

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
