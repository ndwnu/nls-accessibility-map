package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VehiclePropertiesMapperTest {

    private VehiclePropertiesMapper vehiclePropertiesMapper;

    @BeforeEach
    void setUp() {
        vehiclePropertiesMapper = new VehiclePropertiesMapper();
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void map_ok_includeWindowTimes(TrafficSignType trafficSignType) {

        VehicleProperties vehicleProperties = vehiclePropertiesMapper.map(List.of(trafficSignType), true);

        switch (trafficSignType) {
            case C6 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();

            }
            case C7 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
            }
            case C7B -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
            }
            case C12 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();

            }
            case C22C -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isTrue();
            }
        }
        validateVehiclePropertiesDefaultValues(vehicleProperties);
    }


    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void map_ok_notIncludeWindowTimes(TrafficSignType trafficSignType) {

        VehiclePropertiesMapper vehiclePropertiesMapper = new VehiclePropertiesMapper();

        VehicleProperties vehicleProperties = vehiclePropertiesMapper.map(List.of(trafficSignType), false);

        switch (trafficSignType) {
            case C6 -> {
                assertThat(vehicleProperties.carAccessForbidden()).isTrue();
                assertThat(vehicleProperties.hgvAccessForbidden()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbidden()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbidden()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbidden()).isFalse();

                assertThat(vehicleProperties.carAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();

            }
            case C7 -> {
                assertThat(vehicleProperties.carAccessForbidden()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbidden()).isTrue();
                assertThat(vehicleProperties.hgvAndBusAccessForbidden()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbidden()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbidden()).isFalse();

                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();

            }
            case C7B -> {
                assertThat(vehicleProperties.carAccessForbidden()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbidden()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbidden()).isTrue();
                assertThat(vehicleProperties.motorVehicleAccessForbidden()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbidden()).isFalse();

                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();

            }
            case C12 -> {
                assertThat(vehicleProperties.carAccessForbidden()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbidden()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbidden()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbidden()).isTrue();
                assertThat(vehicleProperties.lcvAndHgvAccessForbidden()).isFalse();

                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();

            }
            case C22C -> {
                assertThat(vehicleProperties.carAccessForbidden()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbidden()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbidden()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbidden()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbidden()).isTrue();

                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isTrue();

            }
        }
        validateVehiclePropertiesDefaultValuesWT(vehicleProperties);
    }


    private void validateVehiclePropertiesDefaultValues(VehicleProperties vehicleProperties) {
        assertThat(vehicleProperties.hgvAccessForbidden()).isFalse();
        assertThat(vehicleProperties.busAccessForbidden()).isFalse();
        assertThat(vehicleProperties.hgvAndBusAccessForbidden()).isFalse();
        assertThat(vehicleProperties.tractorAccessForbidden()).isFalse();
        assertThat(vehicleProperties.slowVehicleAccessForbidden()).isFalse();
        assertThat(vehicleProperties.trailerAccessForbidden()).isFalse();
        assertThat(vehicleProperties.motorcycleAccessForbidden()).isFalse();
        assertThat(vehicleProperties.motorVehicleAccessForbidden()).isFalse();
        assertThat(vehicleProperties.lcvAndHgvAccessForbidden()).isFalse();
    }


    private void validateVehiclePropertiesDefaultValuesWT(VehicleProperties vehicleProperties) {

    }
}
