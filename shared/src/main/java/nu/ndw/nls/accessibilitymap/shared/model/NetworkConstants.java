package nu.ndw.nls.accessibilitymap.shared.model;

import com.graphhopper.config.Profile;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class NetworkConstants {

    public static final String VEHICLE_NAME_CAR = "car";
    public static final Profile PROFILE = new Profile(VEHICLE_NAME_CAR);
}
