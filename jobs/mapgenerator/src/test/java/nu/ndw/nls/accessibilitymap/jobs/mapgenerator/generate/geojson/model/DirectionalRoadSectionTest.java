package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DirectionalRoadSectionTest {

    @Test
    void isBackwards_isBackwardsTrue() {
        assertThat(DirectionalRoadSection.builder()
                .roadSectionId(-1)
                .build().isBackwards()).isTrue();
    }

    @Test
    void isBackwards_isBackwardsFalse() {
        assertThat(DirectionalRoadSection.builder()
                .roadSectionId(1)
                .build().isBackwards()).isFalse();
    }
}