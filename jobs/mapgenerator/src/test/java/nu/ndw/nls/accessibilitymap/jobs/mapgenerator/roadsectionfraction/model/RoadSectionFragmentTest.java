package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RoadSectionFragmentTest {

    @Test
    void builder_ok_maxFractions() {
        assertThat(RoadSectionFragment.builder()
                    .fromFraction(0)
                    .toFraction(1)
                    .data(true)
                .build()).isEqualTo(new RoadSectionFragment<>(0, 1, true));
    }

    @Test
    void builder_fail_fromExceedsMinimumFraction() {
        assertThatThrownBy(() -> RoadSectionFragment.builder()
                .fromFraction(-0.1)
                .toFraction(1)
                .data(true)
                .build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("From fraction should be between 0 and 1, but was: -0.1");
    }

    @Test
    void builder_fail_fromExceedsMaximumFraction() {
        assertThatThrownBy(() -> RoadSectionFragment.builder()
                .fromFraction(1.1)
                .toFraction(2)
                .data(true)
                .build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("From fraction should be between 0 and 1, but was: 1.1");
    }

    @Test
    void builder_fail_toExceedsMinimumFraction() {
        assertThatThrownBy(() -> RoadSectionFragment.builder()
                .fromFraction(0)
                .toFraction(-0.1)
                .data(true)
                .build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("To fraction should be between 0 and 1, but was: -0.1");
    }

    @Test
    void builder_fail_toExceedsMaximumFraction() {
        assertThatThrownBy(() -> RoadSectionFragment.builder()
                .fromFraction(0)
                .toFraction(1.1)
                .data(true)
                .build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("To fraction should be between 0 and 1, but was: 1.1");
    }



    @Test
    void builder_fail_fromFractionAfterTwo() {
        assertThatThrownBy(() -> RoadSectionFragment.builder()
                .fromFraction(1)
                .toFraction(0)
                .data(true)
                .build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("From fraction should be less than to fraction, but was from: 1.0 to: 0.0");
    }
}