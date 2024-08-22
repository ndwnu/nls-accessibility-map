package nu.ndw.nls.accessibilitymap.accessibility.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;


class WindowTimeEncodedValueTest {

    @Test
    void value_ok_c6() {
        assertThat(WindowTimeEncodedValue.C6.getEncodedValue()).isEqualTo("car_access_forbidden_windowed");
    }

    @Test
    void value_ok_c7() {
        assertThat(WindowTimeEncodedValue.C7.getEncodedValue()).isEqualTo("hgv_access_forbidden_windowed");
    }

    @Test
    void value_ok_c7b() {
        assertThat(WindowTimeEncodedValue.C7B.getEncodedValue()).isEqualTo("hgv_and_bus_access_forbidden_windowed");
    }

    @Test
    void value_ok_C12() {
        assertThat(WindowTimeEncodedValue.C12.getEncodedValue()).isEqualTo("motor_vehicle_access_forbidden_windowed");
    }

    @Test
    void value_ok_C22C() {
        assertThat(WindowTimeEncodedValue.C22C.getEncodedValue()).isEqualTo("lcv_and_hgv_access_forbidden_windowed");
    }

}