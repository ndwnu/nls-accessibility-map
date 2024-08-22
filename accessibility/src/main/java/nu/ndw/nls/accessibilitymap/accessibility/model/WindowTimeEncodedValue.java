package nu.ndw.nls.accessibilitymap.accessibility.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WindowTimeEncodedValue {
    C6("car_access_forbidden_windowed"),
    C7("hgv_access_forbidden_windowed"),
    C7B("hgv_and_bus_access_forbidden_windowed"),
    C12("motor_vehicle_access_forbidden_windowed"),
    C22C("lcv_and_hgv_access_forbidden_windowed");

    private final String encodedValue;

}
