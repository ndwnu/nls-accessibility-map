package nu.ndw.nls.accessibilitymap.shared.model;

import com.graphhopper.config.Profile;
import com.graphhopper.util.CustomModel;

public final class NetworkConstants {

    public static final String CAR = "car";
    private static final String PROFILE_NAME = "motor_vehicle_custom";
    public static final Profile PROFILE = new Profile(PROFILE_NAME).setVehicle(CAR);
    public static final String NETWORK_NAME = "accessibility_latest";

    private NetworkConstants() {}

    public static Profile profileWithCustomModel(CustomModel customModel) {
        return new Profile(PROFILE_NAME)
                .setVehicle(CAR)
                .setCustomModel(customModel);
    }

}
