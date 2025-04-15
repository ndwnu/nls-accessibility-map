package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;
import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.VEHICLE_NAME_CAR;
import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.config.Profile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkConstantsTest {

    @Test
    @SuppressWarnings("java:S3415")
    void staticValues() {

        assertThat(CAR_PROFILE).isEqualTo(new Profile("car"));
        assertThat(VEHICLE_NAME_CAR).isEqualTo("car");
    }
}