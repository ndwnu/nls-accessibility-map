package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleCharacteristicsJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TransportTypeMapperV2Test {

    private TransportTypeMapperV2 transportTypeMapperV2;

    @BeforeEach
    void setup() {
        transportTypeMapperV2 = new TransportTypeMapperV2();
    }

    @ParameterizedTest
    @EnumSource(VehicleTypeJson.class)
    void map(VehicleTypeJson vehicleTypeJson) {
        VehicleCharacteristicsJson vehicleCharacteristicsJson = VehicleCharacteristicsJson.builder()
                .type(vehicleTypeJson)
                .build();

        assertThat(transportTypeMapperV2.map(vehicleCharacteristicsJson)).containsExactly(switch (vehicleTypeJson) {
            case CAR -> TransportType.CAR;
            case TRUCK -> TransportType.TRUCK;
            case TRACTOR -> TransportType.TRACTOR;
            case MOTORCYCLE -> TransportType.MOTORCYCLE;
            case LIGHT_COMMERCIAL_VEHICLE -> TransportType.DELIVERY_VAN;
            case BUS -> TransportType.BUS;
        });
    }

    @Test
    void map_hasTrailer() {
        VehicleCharacteristicsJson vehicleCharacteristicsJson = VehicleCharacteristicsJson.builder()
                .type(VehicleTypeJson.CAR)
                .hasTrailer(true)
                .build();

        assertThat(transportTypeMapperV2.map(vehicleCharacteristicsJson)).containsExactlyInAnyOrder(
                TransportType.CAR,
                TransportType.VEHICLE_WITH_TRAILER);
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void map(TransportType transportType) {

        VehicleTypeJson expectedVehicleTypeJson = switch (transportType) {
            case TransportType.CAR -> VehicleTypeJson.CAR;
            case TransportType.TRUCK -> VehicleTypeJson.TRUCK;
            case TransportType.TRACTOR -> VehicleTypeJson.TRACTOR;
            case TransportType.MOTORCYCLE -> VehicleTypeJson.MOTORCYCLE;
            case TransportType.DELIVERY_VAN -> VehicleTypeJson.LIGHT_COMMERCIAL_VEHICLE;
            case TransportType.BUS -> VehicleTypeJson.BUS;
            default -> null;
        };

        if (Objects.nonNull(expectedVehicleTypeJson)) {
            assertThat(transportTypeMapperV2.map(Set.of(transportType))).containsExactly(expectedVehicleTypeJson);
        } else {
            assertThat(transportTypeMapperV2.map(Set.of(transportType))).isEmpty();
        }
    }
}
