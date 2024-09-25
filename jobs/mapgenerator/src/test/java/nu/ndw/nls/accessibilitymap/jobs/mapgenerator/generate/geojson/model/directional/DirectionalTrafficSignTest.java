package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import org.junit.jupiter.api.Test;

class DirectionalTrafficSignTest {

    private static final double NWB_FRACTION = 0.2;
    private static final double NWB_FRACTION_REVERSE = 1-NWB_FRACTION;

    @Test
    void getFraction_ok_forward() {
        assertThat(DirectionalTrafficSign.builder()
                .direction(Direction.FORWARD)
                .nwbFraction(NWB_FRACTION)
                .build().getFraction()).isEqualTo(NWB_FRACTION);
    }

    @Test
    void getFraction_ok_backwards() {
        assertThat(DirectionalTrafficSign.builder()
                .direction(Direction.BACKWARD)
                .nwbFraction(NWB_FRACTION)
                .build().getFraction()).isEqualTo(NWB_FRACTION_REVERSE);
    }
}
