package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static lombok.AccessLevel.PRIVATE;

import com.graphhopper.config.Profile;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class NetworkConstants {

    public static final String VEHICLE_NAME_CAR = "car";

    public static final Profile CAR_PROFILE = new Profile(VEHICLE_NAME_CAR);
}
