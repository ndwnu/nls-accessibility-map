package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoundingBoxAreaConfigurationTest {

    private BoundingBoxAreaConfiguration boundingBoxAreaConfiguration;

    @BeforeEach
    void setUp() {

        boundingBoxAreaConfiguration = new BoundingBoxAreaConfiguration();
    }

    @Test
    void getSearchDistanceMultiplier_defaultValue() {

        assertThat(boundingBoxAreaConfiguration.getSearchDistanceMultiplier()).isEqualTo(1.5);
    }

    @Test
    void getSearchDistanceGapFromRequestedSearchAreaInMeters_defaultValue() {
        assertThat(boundingBoxAreaConfiguration.getSearchDistanceGapFromRequestedSearchAreaInMeters()).isEqualTo(10_000.0);
    }
}
