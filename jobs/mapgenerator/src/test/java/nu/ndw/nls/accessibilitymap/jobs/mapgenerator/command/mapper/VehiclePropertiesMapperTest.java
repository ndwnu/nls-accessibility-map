package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VehiclePropertiesMapperTest {

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void map(TrafficSignType trafficSignType) {

        VehiclePropertiesMapper vehiclePropertiesMapper = new VehiclePropertiesMapper();

        VehicleProperties vehicleProperties = vehiclePropertiesMapper.map(trafficSignType);

        switch (trafficSignType) {
            case C6 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
            case C7 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
            case C7B -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
            case C12 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
            case C22C -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isTrue();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
        }
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
}